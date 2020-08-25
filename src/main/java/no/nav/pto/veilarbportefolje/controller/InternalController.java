package no.nav.pto.veilarbportefolje.controller;

import no.nav.common.abac.Pep;
import no.nav.common.client.aktorregister.AktorregisterClient;
import no.nav.common.health.HealthCheck;
import no.nav.common.health.HealthCheckUtils;
import no.nav.common.health.selftest.SelfTestCheck;
import no.nav.common.health.selftest.SelfTestUtils;
import no.nav.common.health.selftest.SelftTestCheckResult;
import no.nav.common.health.selftest.SelftestHtmlGenerator;
import no.nav.pto.veilarbportefolje.arenafiler.gr199.ytelser.KopierGR199FraArena;
import no.nav.pto.veilarbportefolje.arenafiler.gr202.tiltak.TiltakHandler;
import no.nav.pto.veilarbportefolje.config.DatabaseConfig;
import no.nav.pto.veilarbportefolje.elastic.ElasticConfig;
import no.nav.pto.veilarbportefolje.kafka.KafkaConfig;
import no.nav.pto.veilarbportefolje.kafka.KafkaHelsesjekk;
import no.nav.pto.veilarbportefolje.krr.DigitalKontaktinformasjonConfig;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.DigitalKontaktinformasjonV1;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static no.nav.common.health.selftest.SelfTestUtils.checkAllParallel;
import static no.nav.pto.veilarbportefolje.elastic.ElasticConfig.FORVENTET_MINIMUM_ANTALL_DOKUMENTER;

@RestController
@RequestMapping("/internal")
public class InternalController {

    private final List<SelfTestCheck> selfTestChecks;

    @Autowired
    public InternalController(
            DigitalKontaktinformasjonV1 dkifV1,
            JdbcTemplate db,
            TiltakHandler tiltakHandler,
            KopierGR199FraArena kopierGR199FraArena,
            AktorregisterClient aktorregisterClient,
            Pep veilarbPep
    ) {
        List<SelfTestCheck> asyncSelftester = List.of(
                new SelfTestCheck(String.format("Sjekker at antall dokumenter > %s", FORVENTET_MINIMUM_ANTALL_DOKUMENTER), false, ElasticConfig::checkHealth),
                new SelfTestCheck("Database for portefolje", true, () -> DatabaseConfig.dbPinger(db)),
                new SelfTestCheck("Aktorregister", true, aktorregisterClient),
                new SelfTestCheck("ABAC", true, veilarbPep.getAbacClient()),
                new SelfTestCheck("Ping av DKIF_V1. Henter reservasjon fra KRR.", false, () -> DigitalKontaktinformasjonConfig.dkifV1Ping(dkifV1)),
                new SelfTestCheck("Sjekker henting av tiltaksfil fra arena over sftp", true, tiltakHandler::sftpTiltakPing),
                new SelfTestCheck("Sjekker henting av ytelser-fil fra arena over sftp", true, kopierGR199FraArena::sftpLopendeYtelserPing)
        );

        List<SelfTestCheck> kafkaSelftester = Arrays.stream(KafkaConfig.Topic.values())
                .map(topic -> new SelfTestCheck("Sjekker at vi får kontakt med partisjonene for " + topic, false, new KafkaHelsesjekk(topic)))
                .collect(Collectors.toList());

        this.selfTestChecks = Stream.concat(asyncSelftester.stream(), kafkaSelftester.stream())
                .collect(Collectors.toList());
    }

    @GetMapping("/isReady")
    public void isReady() {
        List<HealthCheck> healthChecks = Collections.emptyList();

        HealthCheckUtils.findFirstFailingCheck(healthChecks)
                .ifPresent((failedCheck) -> {
                    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
                });
    }

    @GetMapping("/isAlive")
    public void isAlive() {
    }

    @GetMapping("/selftest")
    public ResponseEntity selftest() {
        List<SelftTestCheckResult> results = checkAllParallel(selfTestChecks);
        String html = SelftestHtmlGenerator.generate(results);
        int status = SelfTestUtils.findHttpStatusCode(results, true);

        return ResponseEntity
                .status(status)
                .contentType(MediaType.TEXT_HTML)
                .body(html);
    }
}
