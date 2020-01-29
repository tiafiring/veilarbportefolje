package no.nav.pto.veilarbportefolje.kafka;

import no.nav.pto.veilarbportefolje.domene.KafkaVedtakStatusEndring;
import no.nav.sbl.dialogarena.common.cxf.StsSecurityConstants;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import static no.nav.pto.veilarbportefolje.config.ApplicationConfig.KAFKA_BROKERS_URL_PROPERTY;
import static no.nav.sbl.util.EnvironmentUtils.*;
import static org.apache.kafka.clients.consumer.ConsumerConfig.*;
import static org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG;

import java.util.Arrays;
import java.util.HashMap;

@Configuration
public class KafkaConsumerConfig {

    protected static final String KAFKA_BROKERS = getRequiredProperty(KAFKA_BROKERS_URL_PROPERTY);
    private static final String USERNAME = getRequiredProperty(StsSecurityConstants.SYSTEMUSER_USERNAME);
    private static final String PASSWORD = getRequiredProperty(StsSecurityConstants.SYSTEMUSER_PASSWORD);

    protected static final String KAFKA_CONSUMER_TOPIC = "aapen-oppfolging-vedtakStatusEndring-v1-" + requireEnvironmentName();

    @Bean
    public VedtaksstotteConsumer vedtaksstotteConsumer () {
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(kafkaProperties());
        consumer.subscribe(Arrays.asList(KAFKA_CONSUMER_TOPIC));
        return new VedtaksstotteConsumer(consumer);
    }

    private static HashMap<String, Object> kafkaProperties () {
        HashMap<String, Object>  props = new HashMap<> ();
        props.put(BOOTSTRAP_SERVERS_CONFIG, KAFKA_BROKERS);
        props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_SSL");
        props.put(SaslConfigs.SASL_MECHANISM, "PLAIN");
        props.put(SaslConfigs.SASL_JAAS_CONFIG, "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"" + USERNAME + "\" password=\"" + PASSWORD + "\";");
        props.put(GROUP_ID_CONFIG, "veilarbportefolje-consumer");
        props.put(AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(MAX_POLL_INTERVAL_MS_CONFIG, 5000);
        props.put(KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return props;
    }

}