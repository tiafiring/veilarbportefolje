package no.nav.fo.internal;


import no.nav.brukerdialog.security.pingable.IssoIsAliveHelsesjekk;
import no.nav.brukerdialog.security.pingable.IssoSystemBrukerTokenHelsesjekk;
import no.nav.fo.service.PepClient;
import no.nav.sbl.dialogarena.types.Pingable;
import no.nav.sbl.dialogarena.types.Pingable.Ping.PingMetadata;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.inject.Inject;
import java.io.File;
import java.io.FileNotFoundException;

@Configuration
public class PingConfig {

    @Inject
    private PepClient pep;

    @Value("${loependeytelser.path}")
    String filpath;

    @Value("${loependeytelser.filnavn}")
    String filnavn;

    @Bean
    public Pingable pepPing() {
        PingMetadata metadata = new PingMetadata(
                "ABAC via " + System.getProperty("abac.endpoint.url"),
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
    public Pingable issoPing() { return new IssoIsAliveHelsesjekk(); }

    @Bean
    public Pingable SystemBrukerToken() { return new IssoSystemBrukerTokenHelsesjekk(); }

    @Bean
    public Pingable nfsPing() {
        PingMetadata metadata = new PingMetadata(
                "NFS via" + System.getProperty("loependeytelser.path"),
                "Sjekk om fil med brukere som mottar ytelser ligger på disk",
                true
        );

        return () -> {
            File file = new File(filpath, filnavn);
            if (file.exists()) {
                return Pingable.Ping.lyktes(metadata);
            } else {
                return Pingable.Ping.feilet(metadata, new FileNotFoundException("File not found at " + filpath + filnavn));
            }
        };
    }
}