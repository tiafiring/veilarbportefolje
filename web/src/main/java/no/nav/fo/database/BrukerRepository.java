package no.nav.fo.database;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.control.Try;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import no.nav.fo.domene.*;
import no.nav.fo.util.UnderOppfolgingRegler;
import no.nav.fo.util.sql.SqlUtils;
import no.nav.fo.util.sql.where.WhereClause;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.Optional.empty;
import static java.util.stream.Collectors.*;
import static no.nav.fo.util.DateUtils.timestampFromISO8601;
import static no.nav.fo.util.DbUtils.*;
import static no.nav.fo.util.MetricsUtils.timed;
import static no.nav.fo.util.StreamUtils.batchProcess;
import static no.nav.fo.util.sql.SqlUtils.*;

@Slf4j
public class BrukerRepository {

    public static final String OPPFOLGINGSBRUKER = "OPPFOLGINGSBRUKER";
    public static final String BRUKERDATA = "BRUKER_DATA";
    private final String AKTOERID_TO_PERSONID = "AKTOERID_TO_PERSONID";
    private final String METADATA = "METADATA";
    static final String FORMIDLINGSGRUPPEKODE = "formidlingsgruppekode";
    static final String KVALIFISERINGSGRUPPEKODE = "kvalifiseringsgruppekode";

    @Inject
    JdbcTemplate db;

    @Inject
    private DataSource ds;

    @Inject
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public void updateMetadata(String name, Date date) {
        update(db, METADATA).set(name, date).execute();
    }

    public Try<Oppfolgingstatus> retrieveOppfolgingstatus(PersonId personId) {
        if (personId == null) {
            return Try.failure(new NullPointerException());
        }
        return Try.of(
                () -> db.query(retrieveOppfolgingstatusSql(), new String[]{personId.toString()}, this::mapToOppfolgingstatus)
        ).onFailure(e -> log.warn("Feil ved uthenting av Arena statuskoder for personid {}", personId, e));
    }

    public Try<VeilederId> retrieveVeileder(AktoerId aktoerId) {
        return Try.of(
                () -> select(ds, BRUKERDATA, this::mapToVeilederId)
                        .column("VEILEDERIDENT")
                        .where(WhereClause.equals("AKTOERID", aktoerId.toString()))
                        .execute()
        ).onFailure(e -> log.warn("Fant ikke veileder for bruker med aktoerId {}", aktoerId));
    }

    public Try<String> retrieveEnhet(Fnr fnr) {
        return Try.of(
                () -> select(ds, OPPFOLGINGSBRUKER, this::mapToEnhet)
                        .column("NAV_KONTOR")
                        .where(WhereClause.equals("FODSELSNR", fnr.toString()))
                        .execute()
        ).onFailure(e -> log.warn("Fant ikke oppfølgingsenhet for bruker"));
    }

    public Try<Integer> insertAktoeridToPersonidMapping(AktoerId aktoerId, PersonId personId) {
        return
                Try.of(
                        () -> insert(db, AKTOERID_TO_PERSONID)
                                .value("AKTOERID", aktoerId.toString())
                                .value("PERSONID", personId.toString())
                                .execute()
                ).onFailure(e -> log.info("Kunne ikke inserte mapping Aktoerid {} -> personId {} i databasen: {}", aktoerId, personId, getCauseString(e)));
    }

    public Try<PersonId> retrievePersonid(AktoerId aktoerId) {
        return Try.of(
                () -> select(db.getDataSource(), AKTOERID_TO_PERSONID, this::mapToPersonId)
                        .column("PERSONID")
                        .where(WhereClause.equals("AKTOERID", aktoerId.toString()))
                        .execute()
        ).onFailure(e -> log.warn("Fant ikke personid for aktoerid {}: {}", aktoerId, getCauseString(e)));
    }

    public Try<PersonId> retrievePersonidFromFnr(Fnr fnr) {
        return Try.of(() ->
                select(db.getDataSource(), OPPFOLGINGSBRUKER, this::personIdMapper)
                        .column("PERSON_ID")
                        .where(WhereClause.equals("FODSELSNR", fnr.toString()))
                        .execute()
        ).onFailure(e -> log.warn("Fant ikke personid for fnr: {}", getCauseString(e)));
    }

    public Try<PersonId> deleteBrukerdata(PersonId personId) {
        return Try.of(
                () -> {
                    delete(db.getDataSource(), BRUKERDATA)
                            .where(WhereClause.equals("PERSONID", personId.toString()))
                            .execute();
                    return personId;
                }
        ).onFailure((e) -> log.warn("Kunne ikke slette brukerdata for personid {}", personId.toString(), e));
    }


    /**
     * MAPPING-FUNKSJONER
     */
    @SneakyThrows
    private String mapToEnhet(ResultSet rs) {
        return rs.getString("NAV_KONTOR");
    }

    @SneakyThrows
    private VeilederId mapToVeilederId(ResultSet rs) {
        return VeilederId.of(rs.getString("VEILEDERIDENT"));
    }

    @SneakyThrows
    private Oppfolgingstatus mapToOppfolgingstatus(ResultSet rs) {
        if (rs.isBeforeFirst()) {
            rs.next();
        }
        return new Oppfolgingstatus()
                .setFormidlingsgruppekode(rs.getString(FORMIDLINGSGRUPPEKODE))
                .setServicegruppekode(rs.getString(KVALIFISERINGSGRUPPEKODE))
                .setVeileder(rs.getString("veilederident"))
                .setOppfolgingsbruker(parseJaNei(rs.getString("OPPFOLGING"), "OPPFOLGING"));
    }

    @SneakyThrows
    private PersonId mapToPersonId(ResultSet rs) {
        return PersonId.of(rs.getString("PERSONID"));
    }

    @SneakyThrows
    private PersonId personIdMapper(ResultSet resultSet) {
        return PersonId.of(Integer.toString(resultSet.getBigDecimal("PERSON_ID").intValue()));
    }

    public void prosesserBrukere(Predicate<SolrInputDocument> filter, Consumer<SolrInputDocument> prosess) {
        prosesserBrukere(10000, filter, prosess);
    }

    void prosesserBrukere(int fetchSize, Predicate<SolrInputDocument> filter, Consumer<SolrInputDocument> prosess) {
        db.setFetchSize(fetchSize);
        db.query(retrieveBrukereSQL(), rs -> {
            SolrInputDocument brukerDokument = mapResultSetTilDokument(rs);
            if (filter.test(brukerDokument)) {
                prosess.accept(brukerDokument);
            }
        });
    }


    public List<SolrInputDocument> retrieveOppdaterteBrukere() {
        List<SolrInputDocument> brukere = new ArrayList<>();
        db.setFetchSize(10000);
        db.query(retrieveOppdaterteBrukereSQL(), rs -> {
            brukere.add(mapResultSetTilDokument(rs));
        });
        return brukere;
    }

    public SolrInputDocument retrieveBrukermedBrukerdata(String personId) {
        String[] args = new String[]{personId};
        return db.query(retrieveBrukerMedBrukerdataSQL(), args, (rs) -> {
            if (rs.isBeforeFirst()) {
                rs.next();
            }
            return mapResultSetTilDokument(rs);
        });
    }

    public List<Brukerdata> retrieveBrukerdata(List<String> personIds) {
        Map<String, Object> params = new HashMap<>();
        params.put("fnrs", personIds);
        return namedParameterJdbcTemplate.queryForList(retrieveBrukerdataSQL(), params)
                .stream()
                .map(data -> new Brukerdata()
                        .setAktoerid((String) data.get("AKTOERID"))
                        .setVeileder((String) data.get("VEILEDERIDENT"))
                        .setPersonid((String) data.get("PERSONID"))
                        .setTildeltTidspunkt((Timestamp) data.get("TILDELT_TIDSPUNKT"))
                        .setYtelse(ytelsemappingOrNull((String) data.get("YTELSE")))
                        .setUtlopsdato(toLocalDateTime((Timestamp) data.get("UTLOPSDATO")))
                        .setUtlopsFasett(manedmappingOrNull((String) data.get("UTLOPSDATOFASETT")))
                        .setDagputlopUke(intValue(data.get("DAGPUTLOPUKE")))
                        .setDagputlopUkeFasett(dagpengerUkeFasettMappingOrNull((String) data.get("DAGPUTLOPUKEFASETT")))
                        .setPermutlopUke(intValue(data.get("PERMUTLOPUKE")))
                        .setPermutlopUkeFasett(dagpengerUkeFasettMappingOrNull((String) data.get("PERMUTLOPUKEFASETT")))
                        .setAapmaxtidUke(intValue(data.get("AAPMAXTIDUKE")))
                        .setAapmaxtidUkeFasett(aapMaxtidUkeFasettMappingOrNull((String) data.get("AAPMAXTIDUKEFASETT")))
                        .setOppfolging(parseJaNei((String) data.get("OPPFOLGING"), "OPPFOLGING"))
                        .setVenterPaSvarFraBruker(toLocalDateTime((Timestamp) data.get("VENTERPASVARFRABRUKER")))
                        .setVenterPaSvarFraNav(toLocalDateTime((Timestamp) data.get("VENTERPASVARFRANAV")))
                        .setIAvtaltAktivitet(parse0OR1((String) data.get("IAVTALTAKTIVITET")))
                        .setNyesteUtlopteAktivitet((Timestamp) data.get("NYESTEUTLOPTEAKTIVITET")))
                .collect(toList());
    }

    public int updateTidsstempel(Timestamp tidsstempel) {
        return db.update(updateTidsstempelSQL(), tidsstempel);
    }

    public java.util.List<Map<String, Object>> retrieveBruker(String aktoerId) {
        return db.queryForList(retrieveBrukerSQL(), aktoerId);
    }

    public java.util.List<Map<String, Object>> retrievePersonid(String aktoerId) {
        return db.queryForList(getPersonidFromAktoeridSQL(), aktoerId);
    }

    public Map<String, Optional<String>> retrievePersonidFromFnrs(Collection<String> fnrs) {
        Map<String, Optional<String>> brukere = new HashMap<>(fnrs.size());

        batchProcess(1000, fnrs, timed("GR199.brukersjekk.batch", (fnrBatch) -> {
            Map<String, Object> params = new HashMap<>();
            params.put("fnrs", fnrBatch);

            Map<String, Optional<String>> fnrPersonIdMap = namedParameterJdbcTemplate.queryForList(
                    getPersonIdsFromFnrsSQL(),
                    params)
                    .stream()
                    .map((rs) -> Tuple.of(
                            (String) rs.get("FODSELSNR"),
                            rs.get("PERSON_ID").toString())
                    )
                    .collect(Collectors.toMap(Tuple2::_1, personData -> Optional.of(personData._2())));

            brukere.putAll(fnrPersonIdMap);
        }));

        fnrs.stream()
                .filter(not(brukere::containsKey))
                .forEach((ikkeFunnetBruker) -> brukere.put(ikkeFunnetBruker, empty()));

        return brukere;
    }

    public Map<PersonId, Optional<AktoerId>> hentAktoeridsForPersonids(List<PersonId> personIds) {
        Map<PersonId, Optional<AktoerId>> brukere = new HashMap<>(personIds.size());

        batchProcess(1000, personIds, timed("retreive.aktoerid.batch",(personIdsBatch) -> {
            Map<String, Object> params = new HashMap<>();
            params.put("personids", personIdsBatch.stream().map(PersonId::toString).collect(toList()));

            Map<PersonId, Optional<AktoerId>> personIdToAktoeridMap = namedParameterJdbcTemplate.queryForList(
                    hentAktoeridsForPersonidsSQL(),params)
                    .stream()
                    .map((rs) -> Tuple.of(PersonId.of((String) rs.get("PERSONID")), AktoerId.of((String) rs.get("AKTOERID")))
                    )
                    .collect(Collectors.toMap(Tuple2::_1, personData -> Optional.of(personData._2())));

            brukere.putAll(personIdToAktoeridMap);
        }));

        personIds.stream()
                .filter(not(brukere::containsKey))
                .forEach((ikkeFunnetBruker) -> brukere.put(ikkeFunnetBruker, empty()));

        return brukere;
    }

    private String hentAktoeridsForPersonidsSQL() {
        return "SELECT " +
                "AKTOERID, " +
                "PERSONID " +
                "FROM " +
                "AKTOERID_TO_PERSONID " +
                "WHERE PERSONID in (:personids)";
    }

    public void setAktiviteterSistOppdatert(String sistOppdatert) {
        db.update("UPDATE METADATA SET aktiviteter_sist_oppdatert = ?", timestampFromISO8601(sistOppdatert));
    }

    void insertOrUpdateBrukerdata(List<Brukerdata> brukerdata, Collection<String> finnesIDb) {
        Map<Boolean, List<Brukerdata>> eksisterendeBrukere = brukerdata
                .stream()
                .collect(groupingBy((data) -> finnesIDb.contains(data.getPersonid())));


        Brukerdata.batchUpdate(db, eksisterendeBrukere.getOrDefault(true, emptyList()));

        eksisterendeBrukere
                .getOrDefault(false, emptyList())
                .forEach(this::upsertBrukerdata);
    }

    void upsertBrukerdata(Brukerdata brukerdata) {
        brukerdata.toUpsertQuery(db).execute();
    }

    public void slettYtelsesdata() {
        SqlUtils.update(db, "bruker_data")
                .set("ytelse", null)
                .set("utlopsdato", null)
                .set("utlopsdatoFasett", null)
                .set("dagputlopuke", null)
                .set("dagputlopukefasett", null)
                .set("permutlopuke", null)
                .set("permutlopukefasett", null)
                .set("aapmaxtiduke", null)
                .set("aapmaxtidukefasett", null)
                .execute();
    }

    private String retrieveOppfolgingstatusSql() {
        return "SELECT " +
                "formidlingsgruppekode, " +
                "kvalifiseringsgruppekode, " +
                "bruker_data.oppfolging, " +
                "veilederident " +
                "FROM " +
                "oppfolgingsbruker " +
                "LEFT JOIN bruker_data " +
                "ON " +
                "bruker_data.personid = oppfolgingsbruker.person_id " +
                "WHERE " +
                "person_id = ? ";
    }

    private static String baseSelect = "SELECT " +
            "person_id, " +
            "fodselsnr, " +
            "fornavn, " +
            "etternavn, " +
            "nav_kontor, " +
            "formidlingsgruppekode, " +
            "iserv_fra_dato, " +
            "kvalifiseringsgruppekode, " +
            "rettighetsgruppekode, " +
            "hovedmaalkode, " +
            "sikkerhetstiltak_type_kode, " +
            "fr_kode, " +
            "sperret_ansatt, " +
            "er_doed, " +
            "doed_fra_dato, " +
            "tidsstempel, " +
            "veilederident, " +
            "ytelse, " +
            "utlopsdato, " +
            "utlopsdatofasett, " +
            "dagputlopuke, dagputlopukefasett, " +
            "permutlopuke, permutlopukefasett, " +
            "aapmaxtiduke, aapmaxtidukefasett, " +
            "oppfolging, " +
            "venterpasvarfrabruker, " +
            "venterpasvarfranav, " +
            "nyesteutlopteaktivitet, " +
            "iavtaltaktivitet " +
            "FROM " +
            "oppfolgingsbruker ";

    String retrieveBrukereSQL() {
        return baseSelect +
                "LEFT JOIN bruker_data " +
                "ON " +
                "bruker_data.personid = oppfolgingsbruker.person_id";

    }

    private String retrieveBrukerMedBrukerdataSQL() {
        return baseSelect +
                "LEFT JOIN bruker_data " +
                "ON " +
                "bruker_data.personid = oppfolgingsbruker.person_id " +
                "WHERE " +
                "person_id = ?";
    }

    String retrieveOppdaterteBrukereSQL() {
        return baseSelect +
                "LEFT JOIN bruker_data " +
                "ON " +
                "bruker_data.personid = oppfolgingsbruker.person_id " +
                "WHERE " +
                "tidsstempel > (" + retrieveSistIndeksertSQL() + ")";
    }

    String retrieveSistIndeksertSQL() {
        return "SELECT SIST_INDEKSERT FROM METADATA";
    }

    String updateTidsstempelSQL() {
        return "UPDATE METADATA SET SIST_INDEKSERT = ?";
    }

    private String getPersonidFromAktoeridSQL() {
        return "SELECT PERSONID FROM AKTOERID_TO_PERSONID WHERE AKTOERID = ?";
    }

    private String getPersonIdsFromFnrsSQL() {
        return
                "SELECT " +
                        "person_id, " +
                        "fodselsnr " +
                        "FROM " +
                        "oppfolgingsbruker " +
                        "WHERE " +
                        "fodselsnr in (:fnrs)";
    }

    private String retrieveBrukerSQL() {
        return "SELECT * FROM BRUKER_DATA WHERE AKTOERID=?";
    }

    private String retrieveBrukerdataSQL() {
        return "SELECT * FROM BRUKER_DATA WHERE PERSONID in (:fnrs)";
    }

    public static boolean erOppfolgingsBruker(SolrInputDocument bruker) {
        return oppfolgingsFlaggSatt(bruker) || erOppfolgingsBrukerIarena(bruker);
    }

    private static boolean erOppfolgingsBrukerIarena(SolrInputDocument bruker) {
        String servicegruppekode = (String) bruker.get("kvalifiseringsgruppekode").getValue();
        String formidlingsgruppekode = (String) bruker.get("formidlingsgruppekode").getValue();
        return UnderOppfolgingRegler.erUnderOppfolging(formidlingsgruppekode, servicegruppekode);
    }

    static boolean oppfolgingsFlaggSatt(SolrInputDocument bruker) {
        return (Boolean) bruker.get("oppfolging").getValue();
    }

    private static Integer intValue(Object value) {
        if (value instanceof BigDecimal) {
            return ((BigDecimal) value).intValue();
        } else if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof String) {
            return Integer.parseInt((String) value);
        } else {
            return null;
        }
    }

    private static LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp != null ? timestamp.toLocalDateTime() : null;
    }

    private ManedFasettMapping manedmappingOrNull(String string) {
        return string != null ? ManedFasettMapping.valueOf(string) : null;
    }

    private YtelseMapping ytelsemappingOrNull(String string) {
        return string != null ? YtelseMapping.valueOf(string) : null;
    }

    private AAPMaxtidUkeFasettMapping aapMaxtidUkeFasettMappingOrNull(String string) {
        return string != null ? AAPMaxtidUkeFasettMapping.valueOf(string) : null;
    }

    private DagpengerUkeFasettMapping dagpengerUkeFasettMappingOrNull(String string) {
        return string != null ? DagpengerUkeFasettMapping.valueOf(string) : null;
    }
}