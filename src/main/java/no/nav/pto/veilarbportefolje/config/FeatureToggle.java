package no.nav.pto.veilarbportefolje.config;


import no.nav.pto.veilarbportefolje.service.UnleashService;

public class FeatureToggle {

    private FeatureToggle() {
    }

    public static final String ALIAS_INDEKSERING = "veilarbportefolje.aliasIndeksering";
    public static final String KAFKA_AIVEN_CONSUMERS_STOP = "veilarbportefolje.kafka_aiven_consumers_stop";
    public static final String KAFKA_ONPREM_CONSUMERS_STOP = "veilarbportefolje.kafka_onprem_consumers_stop";

    public static final String OPPFOLGINGS_BRUKER = "veilarbportefolje.oppfolgingsbruker_pa_postgres";

    public static final String NOM_SKJERMING = "veilarbportefolje.NOM_Skjerming";
    public static final String PDL_BRUKERDATA = "veilarbportefolje.Pdl_brukerdata";
    public static final String PDL_BRUKERDATA_BACKUP = "veilarbportefolje.Pdl_brukerdata_backup";

    public static final String PDL_BRUKERDATA_TOPIC_FOR_IDENTER = "veilarbportefolje.Pdl_brukerdata_topic_for_identer";

    public static boolean brukAvAliasIndeksering(UnleashService unleashService) {
        return unleashService.isEnabled(FeatureToggle.ALIAS_INDEKSERING);
    }

    public static boolean brukNOMSkjerming(UnleashService unleashService) {
        return unleashService.isEnabled(FeatureToggle.NOM_SKJERMING);
    }

    public static boolean brukOppfolgingsbrukerPaPostgres(UnleashService unleashService) {
        return unleashService.isEnabled(FeatureToggle.OPPFOLGINGS_BRUKER);
    }

    public static boolean brukPDLBrukerdata(UnleashService unleashService) {
        return unleashService.isEnabled(FeatureToggle.PDL_BRUKERDATA);
    }

    public static boolean brukArenaSomBackup(UnleashService unleashService) {
        return unleashService.isEnabled(FeatureToggle.PDL_BRUKERDATA_BACKUP);
    }

    public static boolean brukBrukerDataTopicForIdenter(UnleashService unleashService) {
        return unleashService.isEnabled(FeatureToggle.PDL_BRUKERDATA_TOPIC_FOR_IDENTER);
    }
}
