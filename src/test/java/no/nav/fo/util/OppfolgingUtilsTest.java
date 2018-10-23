package no.nav.fo.util;

import no.nav.fo.domene.VurderingsBehov;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class OppfolgingUtilsTest {

    @Test
    public void brukerSkalVaereUnderOppfolging() {
        assertTrue(OppfolgingUtils.erBrukerUnderOppfolging("ARBS", "DUMMY", true));
    }

    @Test
    public void brukerSkalVaereUnderOppfolging2() {
        assertTrue(OppfolgingUtils.erBrukerUnderOppfolging("DUMMY", "DUMMY", true));
    }

    @Test
    public void brukerSkalIKKEVaereUnderOppfolging1() {
        assertFalse(OppfolgingUtils.erBrukerUnderOppfolging("DUMMY", "DUMMY", false));
    }

    @Test
    public void brukerTrengerVurdering() {
        assertTrue(OppfolgingUtils.trengerVurdering("IARBS", "BKART"));
        assertTrue(OppfolgingUtils.trengerVurdering("IARBS", "IVURD"));
    }
    @Test
    public void brukerMedISERVTrengerIkkeVurdering() {
        assertFalse(OppfolgingUtils.trengerVurdering("ISERV", "IVURD"));
        assertFalse(OppfolgingUtils.trengerVurdering("ISERV", "BKART"));
    }

    @Test
    public void brukerUtenBKART_IVURD_trengerIkkeVurdering() {
        assertFalse(OppfolgingUtils.trengerVurdering("IARBS", "VURDU"));
    }

    @Test
    public void brukerMedIServHarIkkeVurderingsBehov() {
        assertThat(OppfolgingUtils.vurderingsBehov("ISERV", "BKART")).isNull();
        assertThat(OppfolgingUtils.vurderingsBehov("ISERV", "IVURD")).isNull();
        assertThat(OppfolgingUtils.vurderingsBehov("ISERV", "VURDU")).isNull();
    }

    @Test
    public void brukerUtenIServOgBKARTHarAEVBehov() {
        assertThat(OppfolgingUtils.vurderingsBehov("IARBS", "BKART")).isEqualTo(VurderingsBehov.ARBEIDSEVNE_VURDERING);
    }

    @Test
    public void brukerUtenIServOgIVURDTHarAEVBehov() {
        assertThat(OppfolgingUtils.vurderingsBehov("IARBS", "IVURD")).isEqualTo(VurderingsBehov.IKKE_VURDERT);
    }

    @Test
    public void brukerUtenIServOgUkjentKodeHarIkkeVurderingsBehov() {
        assertThat(OppfolgingUtils.vurderingsBehov("IARBS", "VURDU")).isNull();
    }
}