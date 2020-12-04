package no.nav.pto.veilarbportefolje.oppfolging;

import no.nav.common.featuretoggle.UnleashService;
import no.nav.common.json.JsonUtils;
import no.nav.pto.veilarbportefolje.arbeidsliste.ArbeidslisteService;
import no.nav.pto.veilarbportefolje.domene.value.AktoerId;
import no.nav.pto.veilarbportefolje.elastic.ElasticServiceV2;
import no.nav.pto.veilarbportefolje.kafka.KafkaConsumerService;
import org.springframework.stereotype.Service;

import static no.nav.pto.veilarbportefolje.config.FeatureToggle.KAFKA_OPPFOLGING;

@Service
public class VeilederTilordnetService implements KafkaConsumerService<String> {

    private final OppfolgingRepository oppfolgingRepository;
    private final ArbeidslisteService arbeidslisteService;
    private final ElasticServiceV2 elasticServiceV2;
    private final UnleashService unleashService;

    public VeilederTilordnetService(OppfolgingRepository oppfolgingRepository, ArbeidslisteService arbeidslisteService, ElasticServiceV2 elasticServiceV2, UnleashService unleashService) {
        this.oppfolgingRepository = oppfolgingRepository;
        this.arbeidslisteService = arbeidslisteService;
        this.elasticServiceV2 = elasticServiceV2;
        this.unleashService = unleashService;
    }

    @Override
    public void behandleKafkaMelding(String kafkaMelding) {
        if (!unleashService.isEnabled(KAFKA_OPPFOLGING)) {
            return;
        }
        final VeilederTilordnetDTO dto = JsonUtils.fromJson(kafkaMelding, VeilederTilordnetDTO.class);
        final AktoerId aktoerId = dto.getAktorId();

        oppfolgingRepository.settVeileder(aktoerId, dto.getVeilederId());
        elasticServiceV2.oppdaterVeileder(aktoerId, dto.getVeilederId());

        final boolean harByttetNavKontor = arbeidslisteService.brukerHarByttetNavKontor(aktoerId);
        if (harByttetNavKontor) {
            arbeidslisteService.slettArbeidsliste(aktoerId);
        }
    }

    @Override
    public boolean shouldRewind() {
        return false;
    }

    @Override
    public void setRewind(boolean rewind) {

    }
}
