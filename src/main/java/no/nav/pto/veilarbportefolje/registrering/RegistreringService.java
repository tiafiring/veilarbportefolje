package no.nav.pto.veilarbportefolje.registrering;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.arbeid.soker.registrering.ArbeidssokerRegistrertEvent;
import no.nav.common.types.identer.AktorId;
import no.nav.pto.veilarbportefolje.elastic.ElasticServiceV2;
import no.nav.pto.veilarbportefolje.kafka.KafkaCommonConsumerService;
import no.nav.pto.veilarbportefolje.kafka.KafkaConsumerService;
import no.nav.pto.veilarbportefolje.service.UnleashService;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicBoolean;

@RequiredArgsConstructor
@Service
@Slf4j
public class RegistreringService extends KafkaCommonConsumerService<ArbeidssokerRegistrertEvent> implements KafkaConsumerService<ArbeidssokerRegistrertEvent> {
    private final RegistreringRepository registreringRepository;
    private final RegistreringRepositoryV2 registreringRepositoryV2;
    private final ElasticServiceV2 elastic;
    private final AtomicBoolean rewind = new AtomicBoolean(false);

    public void behandleKafkaMelding(ArbeidssokerRegistrertEvent kafkaRegistreringMelding) {
        log.info("Oppdaterer registrering på aktør: {}", kafkaRegistreringMelding.getAktorid());
        behandleKafkaMeldingLogikk(kafkaRegistreringMelding);
    }

    public void behandleKafkaMeldingLogikk(ArbeidssokerRegistrertEvent kafkaMelding) {
        registreringRepositoryV2.upsertBrukerRegistrering(kafkaMelding);
        registreringRepository.upsertBrukerRegistrering(kafkaMelding);

        final AktorId aktoerId = AktorId.of(kafkaMelding.getAktorid());
        elastic.updateRegistering(aktoerId, kafkaMelding);
        log.info("Oppdatert utdanningsregistrering for bruker: {}", aktoerId);
    }

    public void slettRegistering(AktorId aktoerId) {
        registreringRepositoryV2.slettBrukerRegistrering(aktoerId);
        registreringRepository.slettBrukerRegistrering(aktoerId);

        log.info("Slettet brukerregistrering for bruker: {}", aktoerId);
    }

    @Override
    public boolean shouldRewind() {
        return rewind.get();
    }

    @Override
    public void setRewind(boolean rewind) {
        this.rewind.set(rewind);
    }
}
