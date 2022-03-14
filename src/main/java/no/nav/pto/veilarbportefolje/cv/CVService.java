package no.nav.pto.veilarbportefolje.cv;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.arbeid.cv.avro.Melding;
import no.nav.arbeid.cv.avro.Meldingstype;
import no.nav.common.types.identer.AktorId;
import no.nav.pto.veilarbportefolje.cv.dto.CVMelding;
import no.nav.pto.veilarbportefolje.kafka.KafkaCommonConsumerService;
import no.nav.pto.veilarbportefolje.opensearch.OpensearchIndexerV2;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Service;

import java.util.List;

import static no.nav.pto.veilarbportefolje.cv.dto.Ressurs.CV_HJEMMEL;


@RequiredArgsConstructor
@Service
@Slf4j
public class CVService extends KafkaCommonConsumerService<Melding> {
    private final OpensearchIndexerV2 opensearchIndexerV2;
    private final CvRepository cvRepository;
    private final CVRepositoryV2 cvRepositoryV2;

    @Override
    public void behandleKafkaMeldingLogikk(Melding kafkaMelding) {
        AktorId aktoerId = AktorId.of(kafkaMelding.getAktoerId());
        boolean cvEksisterer = cvEksistere(kafkaMelding);
        log.info("Oppdater CV eksisterer for bruker: {}, eksisterer: {}", aktoerId.get(), cvEksisterer);

        cvRepositoryV2.upsertCVEksisterer(aktoerId, cvEksisterer);
        cvRepository.upsertCvEksistere(aktoerId, cvEksisterer);

        opensearchIndexerV2.updateCvEksistere(aktoerId, cvEksisterer);
    }

    public void behandleKafkaMeldingCVHjemmel(ConsumerRecord<String, CVMelding> kafkaMelding) {
        log.info(
                "Behandler kafka-melding med key {} og offset {} på topic {}",
                kafkaMelding.key(),
                kafkaMelding.offset(),
                kafkaMelding.topic()
        );
        CVMelding cvMelding = kafkaMelding.value();
        behandleCVHjemmelMelding(cvMelding);
    }

    public void behandleCVHjemmelMelding(CVMelding cvMelding) {
        AktorId aktoerId = cvMelding.getAktoerId();
        boolean harDeltCv = (cvMelding.getSlettetDato() == null);

        if (cvMelding.getRessurs() != CV_HJEMMEL) {
            log.info("Ignorer melding for ressurs {} for bruker {}", cvMelding.getRessurs(), aktoerId);
            return;
        }

        log.info("Oppdaterte bruker: {}. Har delt cv: {}", aktoerId, harDeltCv);
        cvRepositoryV2.upsertHarDeltCv(aktoerId, harDeltCv);
        cvRepository.upsertHarDeltCv(aktoerId, harDeltCv);

        opensearchIndexerV2.updateHarDeltCv(aktoerId, harDeltCv);
    }

    public void migrerCVInfo() {
        List<AktorId> aktivCvData = cvRepository.hentAllleBrukereMedLagretCvData();
        aktivCvData.forEach(aktorId -> {
            boolean harDeltCVOracle = cvRepository.harDeltCv(aktorId);
            boolean harCvOracle = cvRepository.harCv(aktorId);

            boolean harDeltCVPostgres = cvRepositoryV2.harDeltCv(aktorId);
            boolean harCvPostgres = cvRepositoryV2.cvEksisterer(aktorId);
            if (harCvOracle != harCvPostgres || harDeltCVOracle != harDeltCVPostgres) {
                if (harDeltCVOracle != harDeltCVPostgres) {
                    cvRepositoryV2.upsertHarDeltCv(aktorId, harDeltCVOracle);
                }
                if (harCvOracle != harCvPostgres) {
                    cvRepositoryV2.upsertCVEksisterer(aktorId, harCvOracle);
                }
                log.info("Migrerer CV for bruker: {}, diff CV eksisterer {}, diff delt CV: {}", aktorId, harCvOracle != harCvPostgres, harDeltCVOracle != harDeltCVPostgres);
            }
        });
        log.info("Migrerer CV jobb er ferdig");
    }

    private boolean cvEksistere(Melding melding) {
        return melding.getMeldingstype() == Meldingstype.ENDRE || melding.getMeldingstype() == Meldingstype.OPPRETT;
    }
}
