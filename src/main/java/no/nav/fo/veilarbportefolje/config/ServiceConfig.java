package no.nav.fo.veilarbportefolje.config;

import no.nav.fo.veilarbportefolje.aktivitet.AktivitetDAO;
import no.nav.fo.veilarbportefolje.database.KrrRepository;
import no.nav.fo.veilarbportefolje.database.PersistentOppdatering;
import no.nav.fo.veilarbportefolje.service.*;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.DigitalKontaktinformasjonV1;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceConfig {

    @Bean
    public PersistentOppdatering persistentOppdatering() {
        return new PersistentOppdatering();
    }

    @Bean
    public ArbeidslisteService arbeidslisteService() {
        return new ArbeidslisteService();
    }

    @Bean
    public AktivitetService aktivitetService(AktoerService aktoerService, AktivitetDAO aktivitetDAO, PersistentOppdatering persistentOppdatering) {
        return new AktivitetService(aktoerService, aktivitetDAO, persistentOppdatering);
    }

    @Bean
    public TiltakService tiltakService() {
        return new TiltakService();
    }

    @Bean
    public KrrService krrService(KrrRepository krrRepository, DigitalKontaktinformasjonV1 dkif) {
        return new KrrService(krrRepository, dkif);
    }

}
