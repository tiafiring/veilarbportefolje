package no.nav.pto.veilarbportefolje.domene;


import io.getunleash.DefaultUnleash;
import no.nav.common.abac.Pep;
import no.nav.common.metrics.MetricsClient;
import no.nav.common.token_client.client.AzureAdOnBehalfOfTokenClient;
import no.nav.common.types.identer.NavIdent;
import no.nav.poao_tilgang.client.Decision;
import no.nav.pto.veilarbportefolje.auth.AuthService;
import no.nav.pto.veilarbportefolje.auth.PoaoTilgangWrapper;
import no.nav.pto.veilarbportefolje.persononinfo.barnUnder18Aar.BarnUnder18AarData;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SensurerBrukerTest {

    @Mock
    private Pep pep;

    private AuthService authService;
    private AzureAdOnBehalfOfTokenClient azureAdOnBehalfOfTokenClient;

    private PoaoTilgangWrapper poaoTilgangWrapper;

    @Before
    public void setUp() {
        DefaultUnleash defaultUnleash = mock(DefaultUnleash.class);
        when(defaultUnleash.isEnabled(anyString())).thenReturn(true);
        poaoTilgangWrapper = mock(PoaoTilgangWrapper.class);
        authService = new AuthService(pep, poaoTilgangWrapper, azureAdOnBehalfOfTokenClient, defaultUnleash, mock(MetricsClient.class));
    }

    @Test
    public void skalIkkeSeKode6Bruker() {
        when(pep.harVeilederTilgangTilKode6(eq(NavIdent.of("X123456")))).thenReturn(false);
        when(poaoTilgangWrapper.harVeilederTilgangTilKode6()).thenReturn(new Decision.Deny("", ""));
        Bruker filtrerteBrukere = authService.fjernKonfidensiellInfoDersomIkkeTilgang(kode6Bruker(), "X123456");
        sjekkAtKonfidensiellDataErVasket(filtrerteBrukere);
    }

    @Test
    public void skalIkkeSeKode7Bruker() {
        when(pep.harVeilederTilgangTilKode7(eq(NavIdent.of("X123456")))).thenReturn(false);
        when(poaoTilgangWrapper.harVeilederTilgangTilKode7()).thenReturn(new Decision.Deny("", ""));
        Bruker filtrerteBrukere = authService.fjernKonfidensiellInfoDersomIkkeTilgang(kode7Bruker(), "X123456");
        sjekkAtKonfidensiellDataErVasket(filtrerteBrukere);
    }

    @Test
    public void skalIkkeSeEgenAnsatt() {
        when(pep.harVeilederTilgangTilEgenAnsatt(eq(NavIdent.of("X123456")))).thenReturn(false);
        when(poaoTilgangWrapper.harVeilederTilgangTilEgenAnsatt()).thenReturn(new Decision.Deny("", ""));
        Bruker filtrerteBrukere = authService.fjernKonfidensiellInfoDersomIkkeTilgang(egenAnsatt(), "X123456");
        sjekkAtKonfidensiellDataErVasket(filtrerteBrukere);
    }

    @Test
    public void skalSeKode6Bruker() {
        when(pep.harVeilederTilgangTilKode6(eq(NavIdent.of("X123456")))).thenReturn(true);
        when(poaoTilgangWrapper.harVeilederTilgangTilKode6()).thenReturn(Decision.Permit.INSTANCE);
        Bruker filtrerteBrukere = authService.fjernKonfidensiellInfoDersomIkkeTilgang(kode6Bruker(), "X123456");
        assertThat(filtrerteBrukere.getFnr()).isEqualTo("11111111111");
        assertThat(filtrerteBrukere.getFornavn()).isEqualTo("fornavnKode6");
        assertThat(filtrerteBrukere.getEtternavn()).isEqualTo("etternanvKode6");
    }

    @Test
    public void skalSeKode7Bruker() {
        when(pep.harVeilederTilgangTilKode7(eq(NavIdent.of("X123456")))).thenReturn(true);
        when(poaoTilgangWrapper.harVeilederTilgangTilKode7()).thenReturn(Decision.Permit.INSTANCE);
        Bruker filtrerteBrukere = authService.fjernKonfidensiellInfoDersomIkkeTilgang(kode7Bruker(), "X123456");
        assertThat(filtrerteBrukere.getFnr()).isEqualTo("11111111111");
        assertThat(filtrerteBrukere.getFornavn()).isEqualTo("fornavnKode7");
        assertThat(filtrerteBrukere.getEtternavn()).isEqualTo("etternanvKode7");
    }

    @Test
    public void skalSeEgenAnsatt() {
        when(pep.harVeilederTilgangTilEgenAnsatt(eq(NavIdent.of("X123456")))).thenReturn(true);
        when(poaoTilgangWrapper.harVeilederTilgangTilEgenAnsatt()).thenReturn(Decision.Permit.INSTANCE);
        Bruker filtrerteBrukere = authService.fjernKonfidensiellInfoDersomIkkeTilgang(egenAnsatt(), "X123456");
        assertThat(filtrerteBrukere.getFnr()).isEqualTo("11111111111");
        assertThat(filtrerteBrukere.getFornavn()).isEqualTo("fornavnKodeEgenAnsatt");
        assertThat(filtrerteBrukere.getEtternavn()).isEqualTo("etternanvEgenAnsatt");
    }

    @Test
    public void skalSeIkkeKonfidensiellBruker() {
        Bruker filtrerteBrukere = authService.fjernKonfidensiellInfoDersomIkkeTilgang(ikkeKonfidensiellBruker(), "X123456");
        assertThat(filtrerteBrukere.getFnr()).isEqualTo("11111111111");
        assertThat(filtrerteBrukere.getFornavn()).isEqualTo("fornavnIkkeKonfidensiellBruker");
        assertThat(filtrerteBrukere.getEtternavn()).isEqualTo("etternanvIkkeKonfidensiellBruker");
    }

    @Test
    public void skalIkkeSeKode6Barn() {
        when(pep.harVeilederTilgangTilKode6(eq(NavIdent.of("X123456")))).thenReturn(false);
        when(poaoTilgangWrapper.harVeilederTilgangTilKode6()).thenReturn(new Decision.Deny("", ""));
        Bruker filtrerteBrukere = authService.fjernKonfidensiellInfoDersomIkkeTilgang(brukerMedKode6Barn(), "X123456");
        sjekkAtBarnMedKode6ErFjernet(filtrerteBrukere);
    }

    @Test
    public void skalIkkeSeKode7Barn() {
        when(pep.harVeilederTilgangTilKode7(eq(NavIdent.of("X123456")))).thenReturn(false);
        when(poaoTilgangWrapper.harVeilederTilgangTilKode7()).thenReturn(new Decision.Deny("", ""));
        Bruker filtrerteBrukere = authService.fjernKonfidensiellInfoDersomIkkeTilgang(brukerMedKode7Barn(), "X123456");
        sjekkAtBarnMedKode7ErFjernet(filtrerteBrukere);
    }

    @Test
    public void skalFjerneKode7BarnMenIkkeKode6() {
        when(pep.harVeilederTilgangTilKode6(eq(NavIdent.of("X123456")))).thenReturn(true);
        when(pep.harVeilederTilgangTilKode7(eq(NavIdent.of("X123456")))).thenReturn(false);
        when(poaoTilgangWrapper.harVeilederTilgangTilKode6()).thenReturn(Decision.Permit.INSTANCE);
        when(poaoTilgangWrapper.harVeilederTilgangTilKode7()).thenReturn(new Decision.Deny("", ""));
        Bruker filtrertBruker = authService.fjernKonfidensiellInfoDersomIkkeTilgang(brukerMedKode6og7Barn(), "X123456");
        sjekkAtBarnMedKode7ErFjernet(filtrertBruker);
        sjekkAtBarnMedKode6IkkeErFjernet(filtrertBruker);
        assertTrue(filtrertBruker.barnUnder18AarData.size() == 2);
    }

    @Test
    public void skalIkkeSeKode19Barn() {
        when(pep.harVeilederTilgangTilKode6(eq(NavIdent.of("X123456")))).thenReturn(false);
        when(poaoTilgangWrapper.harVeilederTilgangTilKode6()).thenReturn(new Decision.Deny("", ""));
        Bruker filtrerteBrukere = authService.fjernKonfidensiellInfoDersomIkkeTilgang(brukerMedKode19Barn(), "X123456");
        sjekkAtBarnMedKode19ErFjernet(filtrerteBrukere);
    }

    @Test
    public void skalSeKode19Barn() {
        when(pep.harVeilederTilgangTilKode6(eq(NavIdent.of("X123456")))).thenReturn(true);
        when(poaoTilgangWrapper.harVeilederTilgangTilKode6()).thenReturn(Decision.Permit.INSTANCE);
        Bruker filtrerteBrukere = authService.fjernKonfidensiellInfoDersomIkkeTilgang(brukerMedKode19Barn(), "X123456");
        sjekkAtBarnMedKode19ErIkkeFjernet(filtrerteBrukere);
    }


    private void sjekkAtKonfidensiellDataErVasket(Bruker bruker) {
        assertThat(bruker.getFnr()).isEqualTo("");
        assertThat(bruker.getEtternavn()).isEqualTo("");
        assertThat(bruker.getFornavn()).isEqualTo("");
        assertThat(bruker.getSkjermetTil()).isNull();
        assertThat(bruker.getFoedeland()).isNull();
        assertThat(bruker.getLandgruppe()).isNull();
        assertThat(bruker.getTalespraaktolk()).isNull();
        assertThat(bruker.getTegnspraaktolk()).isNull();
        assertThat(bruker.getHovedStatsborgerskap()).isNull();
        assertThat(bruker.getBostedBydel()).isNull();
        assertThat(bruker.getBostedKommune()).isNull();
        assertThat(bruker.isHarUtelandsAddresse()).isEqualTo(false);
    }

    private void sjekkAtBarnMedKode6ErFjernet(Bruker bruker) {
        assertTrue(bruker.getBarnUnder18AarData().stream().noneMatch(
                barnUnder18AarData ->
                        barnUnder18AarData.getDiskresjonskode().equals("6")));
    }

    private void sjekkAtBarnMedKode7ErFjernet(Bruker bruker) {
        assertTrue(bruker.getBarnUnder18AarData().stream().filter(x -> x.getDiskresjonskode() != null).noneMatch(
                barnUnder18AarData ->
                        barnUnder18AarData.getDiskresjonskode().equals("7")));
    }

    private void sjekkAtBarnMedKode6IkkeErFjernet(Bruker bruker) {
        assertTrue(bruker.getBarnUnder18AarData().stream().filter(x -> x.getDiskresjonskode() != null).anyMatch(
                barnUnder18AarData ->
                        barnUnder18AarData.getDiskresjonskode().equals("6")));
    }

    private void sjekkAtBarnMedKode19ErFjernet(Bruker bruker) {
        assertTrue(bruker.getBarnUnder18AarData().stream().filter(x -> x.getDiskresjonskode() != null).noneMatch(
                barnUnder18AarData ->
                        barnUnder18AarData.getDiskresjonskode().equals("19")));
    }

    private void sjekkAtBarnMedKode19ErIkkeFjernet(Bruker bruker) {
        assertTrue(bruker.getBarnUnder18AarData().stream().filter(x -> x.getDiskresjonskode() != null).anyMatch(
                barnUnder18AarData ->
                        barnUnder18AarData.getDiskresjonskode().equals("19")));
    }

    private Bruker kode6Bruker() {
        return new Bruker()
                .setFnr("11111111111")
                .setEtternavn("etternanvKode6")
                .setFornavn("fornavnKode6")
                .setDiskresjonskode("6")
                .setBarnUnder18AarData(emptyList());
    }

    private Bruker kode7Bruker() {
        return new Bruker()
                .setFnr("11111111111")
                .setEtternavn("etternanvKode7")
                .setFornavn("fornavnKode7")
                .setDiskresjonskode("7")
                .setBarnUnder18AarData(emptyList());
    }

    private Bruker egenAnsatt() {
        return new Bruker()
                .setFnr("11111111111")
                .setEtternavn("etternanvEgenAnsatt")
                .setFornavn("fornavnKodeEgenAnsatt")
                .setEgenAnsatt(true);
    }

    private Bruker ikkeKonfidensiellBruker() {
        return new Bruker()
                .setFnr("11111111111")
                .setEtternavn("etternanvIkkeKonfidensiellBruker")
                .setFornavn("fornavnIkkeKonfidensiellBruker");
    }

    private Bruker brukerMedKode6Barn() {
        return new Bruker()
                .setFnr("11111111111")
                .setBarnUnder18AarData(List.of(
                        new BarnUnder18AarData(15, "6"),
                        new BarnUnder18AarData(12, "6")
                ));
    }

    private Bruker brukerMedKode7Barn() {
        return new Bruker()
                .setFnr("11111111111")
                .setBarnUnder18AarData(List.of(new BarnUnder18AarData(
                        1, "7"
                )));
    }

    private Bruker brukerMedKode19Barn() {
        return new Bruker()
                .setFnr("11111111111")
                .setBarnUnder18AarData(List.of(
                        new BarnUnder18AarData(15, "19"),
                        new BarnUnder18AarData(12, null),
                        new BarnUnder18AarData(3, null)
                ));
    }


    private Bruker brukerMedKode6og7Barn() {
        return new Bruker()
                .setFnr("11111111111")
                .setBarnUnder18AarData(List.of(
                        new BarnUnder18AarData(11, "6"),
                        new BarnUnder18AarData(15, "7"),
                        new BarnUnder18AarData(3, null)
                ));
    }


}
