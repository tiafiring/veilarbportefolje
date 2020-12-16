package no.nav.pto.veilarbportefolje.oppfolging;

import io.vavr.control.Try;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import no.nav.pto.veilarbportefolje.database.Table;
import no.nav.pto.veilarbportefolje.domene.value.AktoerId;
import no.nav.pto.veilarbportefolje.domene.BrukerOppdatertInformasjon;
import no.nav.pto.veilarbportefolje.domene.value.VeilederId;
import no.nav.sbl.sql.SqlUtils;
import no.nav.sbl.sql.where.WhereClause;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Optional;

import static java.lang.Boolean.TRUE;
import static no.nav.pto.veilarbportefolje.util.DateUtils.toTimestamp;
import static no.nav.pto.veilarbportefolje.util.DbUtils.parseJaNei;

@Slf4j
public class OppfolgingRepository {

    private final JdbcTemplate db;

    public OppfolgingRepository(JdbcTemplate db) {
        this.db = db;
    }

    public boolean settUnderOppfolging(AktoerId aktoerId, ZonedDateTime startDato) {
        return SqlUtils.upsert(db, Table.OPPFOLGING_DATA.TABLE_NAME)
                .set(Table.OPPFOLGING_DATA.AKTOERID, aktoerId.toString())
                .set(Table.OPPFOLGING_DATA.OPPFOLGING, "J")
                .set(Table.OPPFOLGING_DATA.NY_FOR_VEILEDER, "J")
                .set(Table.OPPFOLGING_DATA.STARTDATO, toTimestamp(startDato))
                .where(WhereClause.equals(Table.OPPFOLGING_DATA.AKTOERID, aktoerId.toString()))
                .execute();
    }

    public int settVeileder(AktoerId aktorId, VeilederId veilederId) {
        return SqlUtils.update(db, Table.OPPFOLGING_DATA.TABLE_NAME)
                .set(Table.OPPFOLGING_DATA.VEILEDERIDENT, veilederId.toString())
                .whereEquals(Table.OPPFOLGING_DATA.AKTOERID, aktorId.toString())
                .execute();
    }

    public int settNyForVeileder(AktoerId aktoerId, boolean nyForVeileder) {
        return SqlUtils.update(db, Table.OPPFOLGING_DATA.TABLE_NAME)
                .set(Table.OPPFOLGING_DATA.NY_FOR_VEILEDER, safeToJaNei(nyForVeileder))
                .whereEquals(Table.OPPFOLGING_DATA.AKTOERID, aktoerId.toString())
                .execute();
    }

    public int settManuellStatus(AktoerId aktoerId, boolean manuellStatus) {
        return SqlUtils.update(db, Table.OPPFOLGING_DATA.TABLE_NAME)
                .set(Table.OPPFOLGING_DATA.MANUELL, safeToJaNei(manuellStatus))
                .whereEquals(Table.OPPFOLGING_DATA.AKTOERID, aktoerId.toString())
                .execute();
    }

    public int settOppfolgingTilFalse(AktoerId aktoerId) {
        return SqlUtils.update(db, Table.OPPFOLGING_DATA.TABLE_NAME)
                .set(Table.OPPFOLGING_DATA.OPPFOLGING, safeToJaNei(false))
                .whereEquals(Table.OPPFOLGING_DATA.AKTOERID, aktoerId.toString())
                .execute();
    }

    public void oppdaterOppfolgingData(BrukerOppdatertInformasjon info) {
        SqlUtils.upsert(db, "OPPFOLGING_DATA")
                .set("VEILEDERIDENT", info.getVeileder())
                .set("OPPDATERT_KILDESYSTEM", info.getEndretTimestamp())
                .set("OPPDATERT_PORTEFOLJE", Timestamp.from(Instant.now()))
                .set("OPPFOLGING", safeToJaNei(info.getOppfolging()))
                .set("NY_FOR_VEILEDER", safeToJaNei(info.getNyForVeileder()))
                .set("MANUELL", safeToJaNei(info.getManuell()))
                .set("AKTOERID", info.getAktoerid())
                .set("STARTDATO", info.getStartDato())
                .set("FEED_ID", info.getFeedId())
                .where(WhereClause.equals("AKTOERID", info.getAktoerid()))
                .execute();
    }

    public static String safeToJaNei(Boolean aBoolean) {
        return TRUE.equals(aBoolean) ? "J" : "N";
    }

    public Optional<BrukerOppdatertInformasjon> hentOppfolgingData(AktoerId aktoerId) {
        final BrukerOppdatertInformasjon oppfolging = SqlUtils.select(db, Table.OPPFOLGING_DATA.TABLE_NAME, rs -> mapToBrukerOppdatertInformasjon(rs))
                .column("*")
                .where(WhereClause.equals(Table.OPPFOLGING_DATA.AKTOERID, aktoerId.toString()))
                .execute();

        return Optional.ofNullable(oppfolging);
    }

    @Deprecated
    public Try<BrukerOppdatertInformasjon> retrieveOppfolgingData(AktoerId aktoerId) {
        String id = aktoerId.toString();
        return Try.of(() -> db.queryForObject(
                "SELECT * FROM OPPFOLGING_DATA WHERE AKTOERID = ?",
                new Object[]{id},
                this::mapToBrukerOppdatertInformasjon)
        ).onFailure(e -> log.info("Fant ikke oppfølgingsdata for bruker med aktoerId {}", id));
    }

    private BrukerOppdatertInformasjon mapToBrukerOppdatertInformasjon(ResultSet resultSet) {
        return mapToBrukerOppdatertInformasjon(resultSet, 0);
    }

    @SneakyThrows
    private BrukerOppdatertInformasjon mapToBrukerOppdatertInformasjon(ResultSet rs, int i) {
        return new BrukerOppdatertInformasjon()
                .setAktoerid(rs.getString("AKTOERID"))
                .setEndretTimestamp(rs.getTimestamp("OPPDATERT_KILDESYSTEM"))
                .setNyForVeileder(parseJaNei(rs.getString("NY_FOR_VEILEDER"), "NY_FOR_VEILEDER"))
                .setOppfolging(parseJaNei(rs.getString("OPPFOLGING"), "OPPFOLGING"))
                .setVeileder(rs.getString("VEILEDERIDENT"))
                .setManuell(parseJaNei(rs.getString("MANUELL"), "MANUELL"))
                .setStartDato(rs.getTimestamp("STARTDATO"));
    }

    public void updateOppfolgingFeedId(BigDecimal id) {
        log.info("Oppdaterer feed_id for oppfølging: {}", id);
        SqlUtils.update(db, "METADATA").set("oppfolging_sist_oppdatert_id", id).execute();
    }
}
