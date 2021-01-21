package no.nav.pto.veilarbportefolje.arbeidsliste;

import lombok.Value;
import no.nav.common.client.aktorregister.AktorregisterClient;
import no.nav.pto.veilarbportefolje.config.ApplicationConfigTest;
import no.nav.pto.veilarbportefolje.database.Table;
import no.nav.pto.veilarbportefolje.domene.value.*;
import no.nav.pto.veilarbportefolje.util.TestDataUtils;
import no.nav.sbl.sql.SqlUtils;
import no.nav.sbl.sql.where.WhereClause;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;

import static java.time.Instant.now;
import static no.nav.pto.veilarbportefolje.util.TestDataUtils.randomFnr;
import static no.nav.pto.veilarbportefolje.util.TestDataUtils.randomPersonId;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = ApplicationConfigTest.class)
class ArbeidslisteServiceTest {

    private final ArbeidslisteService arbeidslisteService;
    private final JdbcTemplate jdbcTemplate;
    private final AktorregisterClient aktorregisterClient;
    private AktoerId aktoerId;

    @Autowired
    public ArbeidslisteServiceTest(ArbeidslisteService arbeidslisteService, JdbcTemplate jdbcTemplate, AktorregisterClient aktorregisterClient) {
        this.arbeidslisteService = arbeidslisteService;
        this.jdbcTemplate = jdbcTemplate;
        this.aktorregisterClient = aktorregisterClient;
    }

    @BeforeEach
    void setup() {
        aktoerId = TestDataUtils.randomAktoerId();
        when(aktorregisterClient.hentAktorId(anyString())).thenReturn(aktoerId.toString());
        jdbcTemplate.execute("TRUNCATE TABLE " + Table.ARBEIDSLISTE.TABLE_NAME);
        jdbcTemplate.execute("TRUNCATE TABLE " + Table.OPPFOLGINGSBRUKER.TABLE_NAME);
        jdbcTemplate.execute("TRUNCATE TABLE " + Table.AKTOERID_TO_PERSONID.TABLE_NAME);
    }

    @Test
    void skal_inserte_fnr_i_arbeidslisten() {
        NavKontor excpectedNavKontor = TestDataUtils.randomNavKontor();
        FnrOgNavKontor fnrOgNavKontor = setUpInitialState(aktoerId, excpectedNavKontor);

        String actualFnr = SqlUtils
                .select(jdbcTemplate, Table.ARBEIDSLISTE.TABLE_NAME, rs -> rs.getString(Table.ARBEIDSLISTE.FNR))
                .column(Table.ARBEIDSLISTE.FNR)
                .where(WhereClause.equals(Table.ARBEIDSLISTE.FNR, fnrOgNavKontor.getFnr()))
                .execute();

        assertThat(actualFnr).isEqualTo(fnrOgNavKontor.getFnr());

        NavKontor actualNavKontor = NavKontor.of(arbeidslisteService.hentNavKontorForArbeidsliste(aktoerId).orElseThrow());

        assertThat(actualNavKontor).isEqualTo(excpectedNavKontor);
    }

    private FnrOgNavKontor setUpInitialState(AktoerId aktoerId, NavKontor navKontor) {
        Fnr fnr = randomFnr();
        PersonId personId = randomPersonId();

        SqlUtils
                .insert(jdbcTemplate, Table.OPPFOLGINGSBRUKER.TABLE_NAME)
                .value(Table.OPPFOLGINGSBRUKER.FODSELSNR, fnr.toString())
                .value(Table.OPPFOLGINGSBRUKER.PERSON_ID, personId.toString())
                .value(Table.OPPFOLGINGSBRUKER.NAV_KONTOR, navKontor.toString())
                .execute();

        SqlUtils
                .insert(jdbcTemplate, Table.AKTOERID_TO_PERSONID.TABLE_NAME)
                .value(Table.AKTOERID_TO_PERSONID.AKTOERID, aktoerId.toString())
                .value(Table.AKTOERID_TO_PERSONID.PERSONID, personId.toString())
                .value(Table.AKTOERID_TO_PERSONID.GJELDENE, true)
                .execute();

        ArbeidslisteDTO dto = new ArbeidslisteDTO(fnr)
                .setNavKontorForArbeidsliste("0000")
                .setAktoerId(aktoerId)
                .setVeilederId(VeilederId.of("0"))
                .setFrist(Timestamp.from(now()))
                .setKategori(Arbeidsliste.Kategori.BLA)
                .setOverskrift("foo");

        arbeidslisteService.createArbeidsliste(dto);

        return new FnrOgNavKontor(fnr.toString(), navKontor.toString());
    }

    @Value
    private static class FnrOgNavKontor {
        String fnr;
        String navKontor;
    }

}