package no.nav.pto.veilarbportefolje.huskelapp;

import no.nav.common.types.identer.AktorId;
import no.nav.common.types.identer.EnhetId;
import no.nav.common.types.identer.Fnr;
import no.nav.pto.veilarbportefolje.config.ApplicationConfigTest;
import no.nav.pto.veilarbportefolje.domene.value.VeilederId;
import no.nav.pto.veilarbportefolje.huskelapp.controller.dto.HuskelappOpprettRequest;
import no.nav.pto.veilarbportefolje.huskelapp.controller.dto.HuskelappRedigerRequest;
import no.nav.pto.veilarbportefolje.huskelapp.domain.Huskelapp;
import no.nav.pto.veilarbportefolje.oppfolging.OppfolgingRepositoryV2;
import no.nav.pto.veilarbportefolje.persononinfo.domene.PDLIdent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static java.util.concurrent.ThreadLocalRandom.current;
import static no.nav.pto.veilarbportefolje.util.TestDataUtils.randomFnr;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = ApplicationConfigTest.class)
public class HuskelappRepositoryTest {
    @Autowired
    private HuskelappRepository repo;
    @Autowired
    private OppfolgingRepositoryV2 oppfolgingRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;


    Fnr fnr1 = Fnr.ofValidFnr("01010101010");
    LocalDate frist1 = LocalDate.of(2026, 1, 1);


    private final HuskelappOpprettRequest huskelapp1 = new HuskelappOpprettRequest(fnr1,
            frist1, ("Huskelapp nr.1 sin kommentar"), EnhetId.of("0010"));

    private final HuskelappOpprettRequest huskelapp2 = new HuskelappOpprettRequest(Fnr.ofValidFnr("01010101011"),
            LocalDate.of(2017, 10, 11), ("Huskelapp nr.2 sin kommentar"), EnhetId.of("0010"));

    private final HuskelappOpprettRequest huskelapp3 = new HuskelappOpprettRequest(Fnr.ofValidFnr("01010101015"),
            LocalDate.of(2017, 10, 11), ("Huskelapp nr.3 sin kommentar"), EnhetId.of("0010"));

    private final HuskelappOpprettRequest huskelapp4 = new HuskelappOpprettRequest(Fnr.ofValidFnr("01010101012"),
            LocalDate.of(2017, 10, 11), ("Huskelapp nr.4 sin kommentar"), EnhetId.of("0010"));

    private final HuskelappOpprettRequest huskelappUtenKommentar = new HuskelappOpprettRequest(Fnr.ofValidFnr("01010101013"),
            LocalDate.of(2017, 10, 11), (null), EnhetId.of("0010"));

    private final HuskelappOpprettRequest huskelapp6 = new HuskelappOpprettRequest(Fnr.ofValidFnr("01010101014"),
            LocalDate.of(2017, 10, 11), (null), EnhetId.of("0010"));

    @BeforeEach
    public void setUp() {
        jdbcTemplate.execute("TRUNCATE TABLE HUSKELAPP");
        jdbcTemplate.execute("TRUNCATE TABLE oppfolging_data");
        jdbcTemplate.execute("TRUNCATE TABLE oppfolgingsbruker_arena_v2");
        jdbcTemplate.execute("TRUNCATE TABLE bruker_identer");
    }


    @Test
    public void skalKunneOppretteOgHenteHuskelapp() {
        repo.opprettHuskelapp(huskelapp1, VeilederId.of("Z123456"));
        Optional<Huskelapp> result = repo.hentAktivHuskelapp(fnr1);
        assertThat(result.isPresent()).isTrue();
        Optional<Huskelapp> result2 = repo.hentAktivHuskelapp(result.get().huskelappId());
        assertThat(result2.isPresent()).isTrue();

        assertThat(result.get().enhetId().toString()).isEqualTo("0010").isEqualTo(result2.get().enhetId().toString());
        assertThat(result.get().frist()).isEqualTo(frist1).isEqualTo(result2.get().frist());
    }

    @Test
    public void skalKunneOppretteOgHenteHuskelapp2() {
        repo.opprettHuskelapp(huskelapp1, VeilederId.of("Z123456"));
        repo.opprettHuskelapp(huskelapp2, VeilederId.of("Z123456"));
        LocalDate nyFrist = LocalDate.of(2025, 10, 11);
        Optional<Huskelapp> huskelapp1result = repo.hentAktivHuskelapp(huskelapp1.brukerFnr());
        HuskelappRedigerRequest huskelappRedigerRequest = new HuskelappRedigerRequest(huskelapp1result.get().huskelappId(), huskelapp1.brukerFnr(), nyFrist, "ny kommentar på huskelapp nr.2", EnhetId.of("0010"));
        repo.redigerHuskelapp(huskelappRedigerRequest, VeilederId.of("Z123456"));
        insertOppfolgingsInformasjon();
        List<Huskelapp> result = repo.hentAktivHuskelapp(EnhetId.of("0010"), VeilederId.of("Z123456"));
        assertThat(result.size()).isEqualTo(2);
    }

    private void insertOppfolgingsInformasjon() {
        insertOppfolgingsInformasjon(huskelapp1.brukerFnr(), AktorId.of("456123"), VeilederId.of("Z123456"), huskelapp1.enhetId());
        insertOppfolgingsInformasjon(huskelapp2.brukerFnr(), AktorId.of("123456"), VeilederId.of("Z123456"), huskelapp2.enhetId());
    }

    private void insertOppfolgingsInformasjon(Fnr fnr, AktorId aktorId, VeilederId veilederId, EnhetId navKontor) {
        int person = current().nextInt();
        jdbcTemplate.update("INSERT INTO bruker_identer (person, ident, gruppe, historisk) values (?,?,?, false)",
                person, aktorId.get(), PDLIdent.Gruppe.AKTORID.name());
        jdbcTemplate.update("INSERT INTO bruker_identer (person, ident, gruppe, historisk) values (?,?,?, false)",
                person, fnr.get(), PDLIdent.Gruppe.FOLKEREGISTERIDENT.name());
        jdbcTemplate.update("INSERT INTO oppfolgingsbruker_arena_v2 (fodselsnr, nav_kontor) values (?,?)", fnr.get(), navKontor.get());
        oppfolgingRepository.settUnderOppfolging(aktorId, ZonedDateTime.now());
        oppfolgingRepository.settVeileder(aktorId, veilederId);
    }


    @Test
    public void skalKunneHenteHuskelappUtenKommentar() {
        repo.opprettHuskelapp(huskelappUtenKommentar, VeilederId.of("Z123456"));
        Optional<Huskelapp> result = repo.hentAktivHuskelapp(Fnr.ofValidFnr("01010101013"));
        assertThat(result.isPresent()).isTrue();
        assertThat(result.get().kommentar()).isEqualTo(null);
    }


    @Test
    public void skalKunneRedigereHuskelapp() {
        Fnr fnrBruker = Fnr.ofValidFnr("01010101011");
        LocalDate nyFrist = LocalDate.of(2025, 10, 11);
        repo.opprettHuskelapp(huskelapp2, VeilederId.of("Z123456"));
        Optional<Huskelapp> huskelappFør = repo.hentAktivHuskelapp(fnrBruker);
        assertThat(huskelappFør.isPresent()).isTrue();
        assertThat(huskelappFør.get().kommentar()).isEqualTo("Huskelapp nr.2 sin kommentar");
        HuskelappRedigerRequest huskelappRedigerRequest = new HuskelappRedigerRequest(huskelappFør.get().huskelappId(), fnrBruker, nyFrist, "ny kommentar på huskelapp nr.2", EnhetId.of("0010"));
        repo.redigerHuskelapp(huskelappRedigerRequest, VeilederId.of("Z234567"));
        Optional<Huskelapp> huskelappEtter = repo.hentAktivHuskelapp(fnrBruker);
        assertThat(huskelappEtter.isPresent()).isTrue();
        assertThat(huskelappEtter.get().kommentar()).isEqualTo("ny kommentar på huskelapp nr.2");
        assertThat(huskelappEtter.get().frist()).isEqualTo(nyFrist);
    }

    @Test
    public void skalKunneSletteHuskelapp() {
        Fnr fnrBruker = Fnr.ofValidFnr("01010101011");
        repo.opprettHuskelapp(huskelapp2, VeilederId.of("Z123456"));
        Optional<Huskelapp> huskelapp = repo.hentAktivHuskelapp(fnrBruker);
        assertThat(huskelapp.isPresent()).isTrue();
        assertThat(huskelapp.get().kommentar()).isEqualTo("Huskelapp nr.2 sin kommentar");
        repo.settSisteHuskelappRadIkkeAktiv(huskelapp.get().huskelappId());
        Optional<Huskelapp> huskelappEtter = repo.hentAktivHuskelapp(fnrBruker);
        assertThat(huskelappEtter.isPresent()).isFalse();
    }

    @Test
    public void skalKunneInaktivereNyesteHuskelappRadNaarFlereRader() {
        Fnr fnrBruker = Fnr.ofValidFnr("01010101011");
        repo.opprettHuskelapp(huskelapp2, VeilederId.of("Z123456"));
        Optional<Huskelapp> huskelappFoer = repo.hentAktivHuskelapp(fnrBruker);
        assertThat(huskelappFoer.isPresent()).isTrue();
        assertThat(huskelappFoer.get().kommentar()).isEqualTo("Huskelapp nr.2 sin kommentar");
        HuskelappRedigerRequest huskelappRedigerRequest = new HuskelappRedigerRequest(huskelappFoer.get().huskelappId(), fnrBruker, huskelappFoer.get().frist(), "ny kommentar på huskelapp nr.2", EnhetId.of("0010"));
        repo.redigerHuskelapp(huskelappRedigerRequest, VeilederId.of("Z123456"));
        List<Huskelapp> alleHuskelappRader = repo.hentAlleRaderPaHuskelapp(huskelappFoer.get().huskelappId());
        repo.settSisteHuskelappRadIkkeAktiv(huskelappFoer.get().huskelappId());
        Optional<Huskelapp> huskelappEtter = repo.hentAktivHuskelapp(fnrBruker);
        assertThat(huskelappEtter.isPresent()).isFalse();
    }


    @Test
    public void sletterAlleHuskelappRader() {
        Fnr fnrBruker = Fnr.ofValidFnr("01010101011");
        repo.opprettHuskelapp(huskelapp2, VeilederId.of("Z123456"));
        Optional<Huskelapp> huskelappFoer = repo.hentAktivHuskelapp(fnrBruker);
        assertThat(huskelappFoer.isPresent()).isTrue();
        assertThat(huskelappFoer.get().kommentar()).isEqualTo("Huskelapp nr.2 sin kommentar");
        HuskelappRedigerRequest huskelappRedigerRequest = new HuskelappRedigerRequest(huskelappFoer.get().huskelappId(), fnrBruker, huskelappFoer.get().frist(), "ny kommentar på huskelapp nr.2", EnhetId.of("0010"));
        repo.redigerHuskelapp(huskelappRedigerRequest, VeilederId.of("Z123456"));
        repo.redigerHuskelapp(huskelappRedigerRequest, VeilederId.of("Z123456"));
        repo.redigerHuskelapp(huskelappRedigerRequest, VeilederId.of("Z123456"));
        List<Huskelapp> alleHuskelappRader = repo.hentAlleRaderPaHuskelapp(huskelappFoer.get().huskelappId());
        assertThat(alleHuskelappRader.size()).isEqualTo(4);
        repo.slettAlleHuskelappRaderPaaBruker(fnrBruker);
        Optional<Huskelapp> huskelappEtter = repo.hentAktivHuskelapp(fnrBruker);
        List<Huskelapp> alleHuskelappRader2 = repo.hentAlleRaderPaHuskelapp(huskelappFoer.get().huskelappId());
        assertThat(alleHuskelappRader2.size()).isEqualTo(0);
    }


    @Test
    public void faarHentetNavkontorPaHuskelapp() {
        Fnr fnrBruker = Fnr.ofValidFnr("01010101011");
        repo.opprettHuskelapp(huskelapp2, VeilederId.of("Z123456"));
        Optional<String> enhetId = repo.hentNavkontorPaHuskelapp(fnrBruker);
        assertThat(enhetId.isPresent()).isTrue();
        assertThat(enhetId.get()).isEqualTo("0010");
    }


    //Slett huskelapp av annen veileder -sjekk at det ikke er ok
    //Bør frist ikke ha lov til å være bakover i tid, eller?


    /*
    @Test
    public void skalKunneOppdatereArbeidslisterUtenKommentar() {
        insertArbeidslister();

        Try<Arbeidsliste> result = repo.retrieveArbeidsliste(data3.getAktorId());
        assertThat(data3.kommentar).isEqualTo(result.get().getKommentar());

        Try<Arbeidsliste> updatedArbeidslisteUtenKommentar = result
                .map(arbeidsliste -> new ArbeidslisteDTO(data3.fnr)
                        .setAktorId(data3.getAktorId())
                        .setVeilederId(data3.getVeilederId())
                        .setEndringstidspunkt(data3.getEndringstidspunkt())
                        .setFrist(data3.getFrist())
                        .setKommentar(null)
                        .setKategori(Arbeidsliste.Kategori.BLA))
                .flatMap(oppdatertArbeidsliste -> repo.updateArbeidsliste(oppdatertArbeidsliste))
                .flatMap(arbeidslisteDTO -> repo.retrieveArbeidsliste(arbeidslisteDTO.getAktorId()));

        assertThat(result.isSuccess()).isTrue();
        assertThat(updatedArbeidslisteUtenKommentar.get().getKommentar()).isEqualTo(null);

    }

    @Test
    public void skalKunneOppdatereArbeidslisterUtenTittel() {
        insertArbeidslister();

        Try<Arbeidsliste> result = repo.retrieveArbeidsliste(data3.getAktorId());
        assertThat(data3.overskrift).isEqualTo(result.get().getOverskrift());

        Try<Arbeidsliste> updatedArbeidslisteUtenTittel = result
                .map(arbeidsliste -> new ArbeidslisteDTO(data3.fnr)
                        .setAktorId(data3.getAktorId())
                        .setVeilederId(data3.getVeilederId())
                        .setEndringstidspunkt(data3.getEndringstidspunkt())
                        .setFrist(data3.getFrist())
                        .setKommentar(data3.getKommentar())
                        .setOverskrift(null)
                        .setKategori(Arbeidsliste.Kategori.BLA))
                .flatMap(oppdatertArbeidsliste -> repo.updateArbeidsliste(oppdatertArbeidsliste))
                .flatMap(arbeidslisteDTO -> repo.retrieveArbeidsliste(arbeidslisteDTO.getAktorId()));

        assertThat(result.isSuccess()).isTrue();
        assertThat(updatedArbeidslisteUtenTittel.get().getOverskrift()).isEqualTo(null);
    }

    @Test
    public void skalKunneOppdatereArbeidslisterUtenKommentarEllerTittel() {
        insertArbeidslister();

        Try<Arbeidsliste> result = repo.retrieveArbeidsliste(data3.getAktorId());
        assertThat(data3.kommentar).isEqualTo(result.get().getKommentar());
        assertThat(data3.overskrift).isEqualTo(result.get().getOverskrift());

        Try<Arbeidsliste> updatedArbeidslisteUtenKommentarEllerTittel = result
                .map(arbeidsliste -> new ArbeidslisteDTO(data3.fnr)
                        .setAktorId(data3.getAktorId())
                        .setVeilederId(data3.getVeilederId())
                        .setEndringstidspunkt(data3.getEndringstidspunkt())
                        .setFrist(data3.getFrist())
                        .setKommentar(null)
                        .setOverskrift(null)
                        .setKategori(Arbeidsliste.Kategori.BLA))
                .flatMap(oppdatertArbeidsliste -> repo.updateArbeidsliste(oppdatertArbeidsliste))
                .flatMap(arbeidslisteDTO -> repo.retrieveArbeidsliste(arbeidslisteDTO.getAktorId()));

        assertThat(result.isSuccess()).isTrue();
        assertThat(updatedArbeidslisteUtenKommentarEllerTittel.get().getKommentar()).isEqualTo(null);
        assertThat(updatedArbeidslisteUtenKommentarEllerTittel.get().getOverskrift()).isEqualTo(null);
    }

    @Test
    public void skalKunneOppdatereKategori() {
        insertArbeidslister();

        Try<Arbeidsliste> result = repo.retrieveArbeidsliste(data.getAktorId());
        assertThat(Arbeidsliste.Kategori.BLA).isEqualTo(result.get().getKategori());

        Try<Arbeidsliste> updatedArbeidsliste = result
                .map(arbeidsliste -> new ArbeidslisteDTO(Fnr.ofValidFnr("01010101010"))
                        .setAktorId(data.getAktorId())
                        .setVeilederId(data.getVeilederId())
                        .setEndringstidspunkt(data.getEndringstidspunkt())
                        .setFrist(data.getFrist())
                        .setKommentar(data.getKommentar())
                        .setKategori(Arbeidsliste.Kategori.LILLA))
                .flatMap(oppdatertArbeidsliste -> repo.updateArbeidsliste(oppdatertArbeidsliste))
                .flatMap(arbeidslisteDTO -> repo.retrieveArbeidsliste(arbeidslisteDTO.getAktorId()));

        assertThat(result.isSuccess()).isTrue();
        assertThat(Arbeidsliste.Kategori.LILLA).isEqualTo(updatedArbeidsliste.get().getKategori());
    }


    @Test
    public void skalOppdatereEksisterendeArbeidsliste() {
        insertArbeidslister();

        VeilederId expected = VeilederId.of("TEST_ID");
        repo.updateArbeidsliste(data.setVeilederId(expected));

        Try<Arbeidsliste> result = repo.retrieveArbeidsliste(data.getAktorId());

        assertThat(result.isSuccess()).isTrue();
        assertThat(expected).isEqualTo(result.get().getSistEndretAv());
    }

    @Test
    public void skalSletteEksisterendeArbeidsliste() {
        insertArbeidslister();
        final Integer rowsUpdated = repo.slettArbeidsliste(data.getAktorId());
        assertThat(rowsUpdated).isEqualTo(1);
    }

    @Test
    public void skalReturnereFailureVedFeil() {
        Try<ArbeidslisteDTO> result = repo.insertArbeidsliste(data.setAktorId(null));
        assertThat(result.isFailure()).isTrue();
    }

    @Test
    public void skalSletteArbeidslisteForAktoerids() {
        insertArbeidslister();

        AktorId aktoerId1 = AktorId.of("22222222");
        Try<Arbeidsliste> arbeidsliste = repo.retrieveArbeidsliste(aktoerId1);
        assertThat(arbeidsliste.isSuccess()).isTrue();
        assertThat(arbeidsliste.get()).isNotNull();

        final Integer rowsUpdated = repo.slettArbeidsliste(aktoerId1);
        assertThat(rowsUpdated).isEqualTo(1);

        arbeidsliste = repo.retrieveArbeidsliste(aktoerId1);
        assertThat(arbeidsliste.isSuccess()).isTrue();
        assertThat(arbeidsliste.get()).isNull();
    }

    @Test
    public void hentArbeidslisteForVeilederPaEnhet_filtrerPaEnhet() {
        EnhetId annetNavKontor = EnhetId.of("1111");
        ArbeidslisteDTO arbeidslistePaNyEnhet = new ArbeidslisteDTO(randomFnr())
                .setAktorId(randomAktorId())
                .setVeilederId(data.getVeilederId())
                .setFrist(data.getFrist())
                .setOverskrift(data.getOverskrift())
                .setKategori(data.getKategori())
                .setNavKontorForArbeidsliste(annetNavKontor.get())
                .setKommentar("Arbeidsliste 1 kopi kommentar");

        insertArbeidslister();
        insertOppfolgingsInformasjon();
        insertOppfolgingsInformasjon(arbeidslistePaNyEnhet.getAktorId(), arbeidslistePaNyEnhet.getVeilederId(), annetNavKontor);
        repo.insertArbeidsliste(arbeidslistePaNyEnhet);

        List<Arbeidsliste> arbeidslistes1 = repo.hentArbeidslisteForVeilederPaEnhet(EnhetId.of(data.getNavKontorForArbeidsliste()), data.getVeilederId());
        List<Arbeidsliste> arbeidslistesAnnenEnhet = repo.hentArbeidslisteForVeilederPaEnhet(EnhetId.of(arbeidslistePaNyEnhet.getNavKontorForArbeidsliste()), arbeidslistePaNyEnhet.getVeilederId());

        assertThat(arbeidslistePaNyEnhet.getVeilederId()).isEqualTo(data.getVeilederId());

        assertThat(arbeidslistes1.size()).isEqualTo(1);
        assertThat(arbeidslistesAnnenEnhet.size()).isEqualTo(1);
        assertThat(arbeidslistes1.get(0).getKommentar()).isEqualTo(data.getKommentar());
        assertThat(arbeidslistesAnnenEnhet.get(0).getKommentar()).isEqualTo(arbeidslistePaNyEnhet.getKommentar());
    }

    @Test
    public void hentArbeidslisteForVeilederPaEnhet_filtrerPaVeileder() {
        insertArbeidslister();
        insertOppfolgingsInformasjon();
        List<Arbeidsliste> arbeidslistes1 = repo.hentArbeidslisteForVeilederPaEnhet(EnhetId.of(data.getNavKontorForArbeidsliste()), data.getVeilederId());
        List<Arbeidsliste> arbeidslistes2 = repo.hentArbeidslisteForVeilederPaEnhet(EnhetId.of(data2.getNavKontorForArbeidsliste()), data2.getVeilederId());

        assertThat(arbeidslistes1.size()).isEqualTo(1);
        assertThat(arbeidslistes2.size()).isEqualTo(1);
        assertThat(arbeidslistes1.get(0).getKommentar()).isEqualTo(data.getKommentar());
        assertThat(arbeidslistes2.get(0).getKommentar()).isEqualTo(data2.getKommentar());
    }

    @Test
    public void hentArbeidslisteForVeilederPaEnhet_arbeidslisteKanLagesAvAnnenVeileder() {
        EnhetId navKontor = EnhetId.of(data.getNavKontorForArbeidsliste());
        ArbeidslisteDTO arbeidslisteLagetAvAnnenVeileder = new ArbeidslisteDTO(randomFnr())
                .setAktorId(randomAktorId())
                .setVeilederId(randomVeilederId())
                .setFrist(data.getFrist())
                .setOverskrift(data.getOverskrift())
                .setKategori(data.getKategori())
                .setNavKontorForArbeidsliste(navKontor.get())
                .setKommentar("Arbeidsliste 1 kopi kommentar");
        insertArbeidslister();
        insertOppfolgingsInformasjon();
        repo.insertArbeidsliste(arbeidslisteLagetAvAnnenVeileder);
        insertOppfolgingsInformasjon(arbeidslisteLagetAvAnnenVeileder.getAktorId(), data.getVeilederId(), navKontor);

        List<Arbeidsliste> arbeidslister = repo.hentArbeidslisteForVeilederPaEnhet(navKontor, data.getVeilederId());

        assertThat(arbeidslister.size()).isEqualTo(2);
        assertThat(arbeidslister.stream().anyMatch(x -> x.getKommentar().equals(data.getKommentar()))).isTrue();
        assertThat(arbeidslister.stream().anyMatch(x -> x.getKommentar().equals(arbeidslisteLagetAvAnnenVeileder.getKommentar()))).isTrue();
    }

    private void insertHuskelapper() {
        Try<ArbeidslisteDTO> result1 = repo.insertArbeidsliste(data);
        Try<ArbeidslisteDTO> result2 = repo.insertArbeidsliste(data2);
        Try<ArbeidslisteDTO> result3 = repo.insertArbeidsliste(data3);
        Try<ArbeidslisteDTO> resultUtenTittelData = repo.insertArbeidsliste(utenTittelData);
        Try<ArbeidslisteDTO> resultUtenKommentarData = repo.insertArbeidsliste(utenKommentarData);
        Try<ArbeidslisteDTO> resultUtenTittelEllerKommentarData = repo.insertArbeidsliste(utenTittelellerKommentarData);
        assertThat(result1.isSuccess()).isTrue();
        assertThat(result2.isSuccess()).isTrue();
        assertThat(result3.isSuccess()).isTrue();
        assertThat(resultUtenTittelData.isSuccess()).isTrue();
        assertThat(resultUtenKommentarData.isSuccess()).isTrue();
        assertThat(resultUtenTittelEllerKommentarData.isSuccess()).isTrue();
    }

    private void insertOppfolgingsInformasjon() {
        insertOppfolgingsInformasjon(data.getAktorId(), data.getVeilederId(), EnhetId.of(data.getNavKontorForArbeidsliste()));
        insertOppfolgingsInformasjon(data2.getAktorId(), data2.getVeilederId(), EnhetId.of(data.getNavKontorForArbeidsliste()));
    }

    private void insertOppfolgingsInformasjon(AktorId aktorId, VeilederId veilederId, EnhetId navKontor) {
        int person = current().nextInt();
        Fnr fnr = randomFnr();
        jdbcTemplate.update("INSERT INTO bruker_identer (person, ident, gruppe, historisk) values (?,?,?, false)",
                person, aktorId.get(), PDLIdent.Gruppe.AKTORID.name());
        jdbcTemplate.update("INSERT INTO bruker_identer (person, ident, gruppe, historisk) values (?,?,?, false)",
                person, fnr.get(), PDLIdent.Gruppe.FOLKEREGISTERIDENT.name());
        jdbcTemplate.update("INSERT INTO oppfolgingsbruker_arena_v2 (fodselsnr, nav_kontor) values (?,?)", fnr.get(), navKontor.get());
        oppfolgingRepository.settUnderOppfolging(aktorId, ZonedDateTime.now());
        oppfolgingRepository.settVeileder(aktorId, veilederId);
    }
    */
}
