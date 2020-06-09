package no.nav.pto.veilarbportefolje.feed;

import no.nav.common.auth.Subject;
import no.nav.common.auth.SubjectHandler;
import no.nav.common.auth.TestSubjectUtils;
import org.junit.Test;

import static org.assertj.core.api.Java6Assertions.assertThat;


public class OidcFeedAuthorizationModuleTest {

    @Test
    public void skal_gi_tilgang() {
        System.setProperty("test.feed.brukertilgang", "bruker1,bruker2");
        assertThat(isRequestAuthorized("bruker1")).isTrue();
    }

    @Test
    public void skal_ikke_gi_tilgang() {
        System.setProperty("test.feed.brukertilgang", "bruker1,bruker2");
        assertThat(isRequestAuthorized("bruker3")).isFalse();
    }

    private boolean isRequestAuthorized(String uid) {
        Subject subject = TestSubjectUtils.builder().uid(uid).build();
        return SubjectHandler.withSubject(subject, () -> new OidcFeedAuthorizationModule().isRequestAuthorized("test"));
    }
}