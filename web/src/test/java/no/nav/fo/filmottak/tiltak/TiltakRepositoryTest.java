package no.nav.fo.filmottak.tiltak;

import com.google.common.base.Joiner;
import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;
import io.vavr.collection.List;
import no.nav.fo.config.ApplicationConfigTest;
import no.nav.fo.database.BrukerRepositoryTest;
import no.nav.melding.virksomhet.tiltakogaktiviteterforbrukere.v1.Bruker;
import no.nav.melding.virksomhet.tiltakogaktiviteterforbrukere.v1.Periode;
import no.nav.melding.virksomhet.tiltakogaktiviteterforbrukere.v1.Tiltaksaktivitet;
import no.nav.melding.virksomhet.tiltakogaktiviteterforbrukere.v1.Tiltakstyper;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Arrays;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ApplicationConfigTest.class})
public class TiltakRepositoryTest {

    @Inject
    private JdbcTemplate jdbcTemplate;

    @Inject
    private TiltakRepository tiltakRepository;

    @Before
    public void tomTabeller() {
        jdbcTemplate.execute("truncate table oppfolgingsbruker");
        jdbcTemplate.execute("truncate table brukertiltak");
        jdbcTemplate.execute("truncate table enhettiltak");
        jdbcTemplate.execute("DELETE FROM tiltakkodeverk");
    }

    @Test
    public void skalSletteDataITabeller() throws Exception {
        insertTestData();
        tiltakRepository.slettEnhettiltak();
        tiltakRepository.slettBrukertiltak();
        tiltakRepository.slettTiltakskoder();

        assertThat(jdbcTemplate.queryForList("SELECT * FROM TILTAKKODEVERK").size()).isEqualTo(0);
        assertThat(jdbcTemplate.queryForList("SELECT * FROM BRUKERTILTAK").size()).isEqualTo(0);
        assertThat(jdbcTemplate.queryForList("SELECT * FROM ENHETTILTAK").size()).isEqualTo(0);
    }

    @Test
    public void skalInserteBrukertiltak() {
        insertKodeverk();
        Bruker bruker = mock(Bruker.class);
        when(bruker.getPersonident()).thenReturn("11111111111");
        Tiltaksaktivitet tiltaksaktivitet1 = new Tiltaksaktivitet();
        tiltaksaktivitet1.setTiltakstype("A");
        Periode periode1 = new Periode();
        periode1.setTom(XMLGregorianCalendarImpl.createDateTime(2000, 6, 4, 16, 16,16));
        tiltaksaktivitet1.setDeltakelsePeriode(periode1);
        when(bruker.getTiltaksaktivitetListe()).thenReturn(Arrays.asList(
            tiltaksaktivitet1
        ));

        tiltakRepository.lagreBrukertiltak(bruker);

        assertThat(jdbcTemplate.queryForMap("SELECT * FROM BRUKERTILTAK WHERE FODSELSNR = '11111111111'").keySet()).containsExactly("FODSELSNR", "TILTAKSKODE", "TILDATO");
    }

    @Test
    public void skalInserteBrukertiltakNarPeriodeOgTildatoErNull() {
        insertKodeverk();
        Bruker bruker = mock(Bruker.class);
        when(bruker.getPersonident()).thenReturn("11111111111");
        Tiltaksaktivitet tiltaksaktivitet1 = new Tiltaksaktivitet();
        tiltaksaktivitet1.setTiltakstype("A");
        Tiltaksaktivitet tiltaksaktivitet2 = new Tiltaksaktivitet();
        tiltaksaktivitet2.setTiltakstype("B");
        tiltaksaktivitet2.setDeltakelsePeriode(new Periode());
        when(bruker.getTiltaksaktivitetListe()).thenReturn(Arrays.asList(
            tiltaksaktivitet1,
            tiltaksaktivitet2
        ));

        tiltakRepository.lagreBrukertiltak(bruker);

        assertThat(jdbcTemplate.queryForList("SELECT * FROM BRUKERTILTAK").size()).isEqualTo(2);
    }

    @Test
    public void skalInserteTiltakKodeverk() {
        Tiltakstyper tiltakstype1 = new Tiltakstyper();
        tiltakstype1.setValue("A");
        tiltakstype1.setTermnavn("Tiltak1");
        Tiltakstyper tiltakstype2 = new Tiltakstyper();
        tiltakstype2.setValue("B");
        tiltakstype2.setTermnavn("Tiltak2");

        tiltakRepository.lagreTiltakskoder(tiltakstype1);
        tiltakRepository.lagreTiltakskoder(tiltakstype2);

        assertThat(jdbcTemplate.queryForList("SELECT * FROM TILTAKKODEVERK").size()).isEqualTo(2);
    }

    @Test
    public void skalInserteEnhettiltak() {
        insertKodeverk();
        tiltakRepository.lagreEnhettiltak(Arrays.asList(
            TiltakForEnhet.of("1234", "A"),
            TiltakForEnhet.of("5678", "B"),
            TiltakForEnhet.of("1234", "C")
        ));

        assertThat(jdbcTemplate.queryForList("SELECT * FROM ENHETTILTAK").size()).isEqualTo(3);
    }

    @Test
    public void skalHenteParMedEnhetOgFnr() {
        insertTestData();

        Map<String, java.util.List<String>> enhetMedPersonIder = tiltakRepository.hentEnhetTilFodselsnummereMap();

        assertThat(enhetMedPersonIder.get("0219")).containsExactly("10000000048", "10000000000");
        assertThat(enhetMedPersonIder.get("1102")).containsExactly("10000000020", "10000000008", "10000000063");
        assertThat(enhetMedPersonIder.get("0806")).containsExactly("10000000009");
    }

    @Test
    public void skalMappeDbRaderTilEnhetTilFnrs() {
        java.util.List<EnhetTilFnr> rader = List.of(
            new EnhetTilFnr("0001", "11111111111"),
            new EnhetTilFnr("0002", "22222222222"),
            new EnhetTilFnr("0001", "22222222222"),
            new EnhetTilFnr("0003", "22222222222"),
            new EnhetTilFnr("0003", "11111111111")
        ).toJavaList();

        Map<String, java.util.List<String>> mappedRader = tiltakRepository.mapEnhetTilFnrs(rader);

        assertThat(mappedRader.size()).isEqualTo(3);
        assertThat(mappedRader.get("0001")).containsExactly("11111111111", "22222222222");
        assertThat(mappedRader.get("0002")).containsExactly("22222222222");
        assertThat(mappedRader.get("0003")).containsExactly("22222222222", "11111111111");
    }

    @Test(expected = UndeclaredThrowableException.class)
    public void skalKasteExceptionNarSletterKodeverkForResten() {
        insertTestData();
        tiltakRepository.slettTiltakskoder();
        tiltakRepository.slettEnhettiltak();
        tiltakRepository.slettBrukertiltak();
    }

    private void insertTestData() {
        try {
            jdbcTemplate.execute(Joiner.on("\n").join(IOUtils.readLines(BrukerRepositoryTest.class.getResourceAsStream("/insert-test-data-tiltak.sql"))));
            jdbcTemplate.execute(Joiner.on("\n").join(IOUtils.readLines(BrukerRepositoryTest.class.getResourceAsStream("/insert-test-data-oppfolgingsbruker.sql"))));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void insertKodeverk() {
        jdbcTemplate.execute("INSERT INTO TILTAKKODEVERK (KODE, VERDI) VALUES('A', 'TILTAK1')");
        jdbcTemplate.execute("INSERT INTO TILTAKKODEVERK (KODE, VERDI) VALUES('B', 'TILTAK2')");
        jdbcTemplate.execute("INSERT INTO TILTAKKODEVERK (KODE, VERDI) VALUES('C', 'TILTAK3')");
    }
}