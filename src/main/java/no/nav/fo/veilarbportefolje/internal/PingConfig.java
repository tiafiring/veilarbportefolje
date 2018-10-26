package no.nav.fo.veilarbportefolje.internal;


import no.nav.brukerdialog.security.pingable.IssoIsAliveHelsesjekk;
import no.nav.brukerdialog.security.pingable.IssoSystemBrukerTokenHelsesjekk;
import no.nav.fo.veilarbportefolje.service.PepClient;
import no.nav.sbl.dialogarena.types.Pingable;
import no.nav.sbl.dialogarena.types.Pingable.Ping.PingMetadata;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.inject.Inject;

import static no.nav.sbl.dialogarena.common.abac.pep.service.AbacServiceConfig.ABAC_ENDPOINT_URL_PROPERTY_NAME;
import static no.nav.sbl.util.EnvironmentUtils.getRequiredProperty;

@Configuration
public class PingConfig {

    @Inject
    private PepClient pep;

    @Bean
    public Pingable pepPing() {
        PingMetadata metadata = new PingMetadata(
                "ABAC via " + getRequiredProperty(ABAC_ENDPOINT_URL_PROPERTY_NAME),
                "Tilgangskontroll, sjekk om NAV-ansatt har tilgang til bruker.",
                true
        );

        return () -> {
            try {
                pep.ping();
                return Pingable.Ping.lyktes(metadata);
            } catch (Exception e) {
                return Pingable.Ping.feilet(metadata, e);
            }
        };
    }

    @Bean
    public Pingable issoPing() {
        return new IssoIsAliveHelsesjekk();
    }

    @Bean
    public Pingable SystemBrukerToken() {
        return new IssoSystemBrukerTokenHelsesjekk();
    }
}
