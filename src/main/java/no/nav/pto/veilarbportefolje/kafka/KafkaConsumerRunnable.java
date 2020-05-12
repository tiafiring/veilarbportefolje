package no.nav.pto.veilarbportefolje.kafka;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import no.nav.common.utils.IdUtils;
import no.nav.pto.veilarbportefolje.util.KafkaProperties;
import no.nav.sbl.featuretoggle.unleash.UnleashService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.slf4j.MDC;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.time.Duration.ofSeconds;
import static java.util.Collections.singletonList;
import static no.nav.log.LogFilter.PREFERRED_NAV_CALL_ID_HEADER_NAME;

@Slf4j
public class KafkaConsumerRunnable implements Runnable {

    private final KafkaConsumerService kafkaService;
    private final UnleashService unleashService;
    private final String topic;
    private final Optional<String> featureNavn;
    private final KafkaConsumer<String, String> consumer;
    private final AtomicBoolean shutdown;
    private final CountDownLatch shutdownLatch;

    public KafkaConsumerRunnable(KafkaConsumerService kafkaService,
                                 UnleashService unleashService,
                                 KafkaConfig.Topic topic,
                                 Optional<String> featureNavn) {

        this.kafkaService = kafkaService;
        this.unleashService = unleashService;
        this.topic = topic.topic;
        this.featureNavn = featureNavn;
        this.consumer = new KafkaConsumer<>(KafkaProperties.kafkaProperties(topic));
        this.shutdown = new AtomicBoolean(false);
        this.shutdownLatch = new CountDownLatch(1);

        CompletableFuture.runAsync(this);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> this.shutdown()));
    }

    @Override
    public void run() {
        try {
            consumer.subscribe(singletonList(topic));
            log.info("Subscriber til topic {}", topic);

            while (featureErPa() && !shutdown.get()) {
                ConsumerRecords<String, String> records = consumer.poll(ofSeconds(1));
                records.forEach(this::process);
            }
            log.info("Avslutter konsumering av topic {}", topic);
        } catch (Exception e) {
            log.error("Konsument feilet under poll() eller subscribe() for topic {}", topic);
        } finally {
            consumer.close();
            shutdownLatch.countDown();
            log.info("Lukket konsument");
        }
    }

    @SneakyThrows
    public void shutdown() {
        shutdown.set(true);
        shutdownLatch.await();
    }

    private void process(ConsumerRecord<String, String> record) {
        String correlationId = getCorrelationIdFromHeaders(record.headers());
        MDC.put(PREFERRED_NAV_CALL_ID_HEADER_NAME, correlationId);

        log.info(
                "Behandler kafka-melding med key {} og offset {} på topic {}",
                record.key(),
                record.offset(),
                record.topic()
        );

        try {
            kafkaService.behandleKafkaMelding(record.value());
        } catch (Exception e) {
            log.error(
                    "Behandling av kafka-melding feilet for key {} og offset {} på topic {}",
                    record.key(),
                    record.offset(),
                    record.topic()
            );
        }
        MDC.remove(PREFERRED_NAV_CALL_ID_HEADER_NAME);
    }

    private boolean featureErPa() {
        return this.featureNavn
                .map(unleashService::isEnabled)
                .orElse(false);
    }

    static String getCorrelationIdFromHeaders(Headers headers) {
        return Optional.ofNullable(headers.lastHeader(PREFERRED_NAV_CALL_ID_HEADER_NAME))
                .map(Header::value)
                .map(String::new)
                .orElse(IdUtils.generateId());
    }

}
