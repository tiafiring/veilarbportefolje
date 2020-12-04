package no.nav.pto.veilarbportefolje.oppfolging;

import no.nav.pto.veilarbportefolje.domene.value.AktoerId;
import no.nav.pto.veilarbportefolje.domene.value.VeilederId;
import no.nav.pto.veilarbportefolje.elastic.domene.OppfolgingsBruker;
import no.nav.pto.veilarbportefolje.util.EndToEndTest;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static no.nav.pto.veilarbportefolje.util.ElasticTestClient.pollElasticUntil;
import static no.nav.pto.veilarbportefolje.util.TestDataUtils.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class VeilederTilordnetServiceTest extends EndToEndTest {

    private final VeilederTilordnetService veilederTilordnetService;

    @Autowired
    public VeilederTilordnetServiceTest(VeilederTilordnetService veilederTilordnetService) {
        this.veilederTilordnetService = veilederTilordnetService;
    }

    @Test
    void skal_oppdatere_tilordnet_veileder() {
        final AktoerId aktoerId = randomAktoerId();
        final VeilederId nyVeileder = randomVeilederId();
        final String payload = new JSONObject()
                .put("aktorId", aktoerId.getValue())
                .put("veilederId", nyVeileder.getValue())
                .toString();

        testDataClient.setupBruker(aktoerId, randomNavKontor(), randomVeilederId());

        veilederTilordnetService.behandleKafkaMelding(payload);

        final OppfolgingsBruker bruker = elasticTestClient.hentBrukerFraElastic(aktoerId);
        final VeilederId tilordnetVeileder = VeilederId.of(bruker.getVeileder_id());

        assertThat(tilordnetVeileder).isEqualTo(nyVeileder);
    }

    @Test
    void skal_slette_arbeidsliste_om_bruker_har_byttet_nav_kontor() {
        final AktoerId aktoerId = randomAktoerId();
        final VeilederId nyVeileder = randomVeilederId();
        final String payload = new JSONObject()
                .put("aktorId", aktoerId.getValue())
                .put("veilederId", nyVeileder.getValue())
                .toString();

        testDataClient.setupBrukerMedArbeidsliste(aktoerId, randomNavKontor(), randomVeilederId());
        testDataClient.endreNavKontorForBruker(aktoerId, randomNavKontor());
        final boolean arbeidslisteAktiv = arbeidslisteAktiv(aktoerId);
        assertThat(arbeidslisteAktiv).isTrue();

        veilederTilordnetService.behandleKafkaMelding(payload);
        pollElasticUntil(() -> !arbeidslisteAktiv(aktoerId));
    }

    private Boolean arbeidslisteAktiv(AktoerId aktoerId) {
        return (Boolean) elasticTestClient.getDocument(aktoerId).get().getSourceAsMap().get("arbeidsliste_aktiv");
    }
}
