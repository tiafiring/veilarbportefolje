package no.nav.fo.service;

import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import no.nav.fo.database.BrukerRepository;
import no.nav.fo.domene.AktoerId;
import no.nav.fo.domene.Fnr;
import no.nav.fo.domene.PersonId;
import no.nav.tjeneste.virksomhet.aktoer.v2.AktoerV2;
import no.nav.tjeneste.virksomhet.aktoer.v2.meldinger.WSHentAktoerIdForIdentRequest;
import no.nav.tjeneste.virksomhet.aktoer.v2.meldinger.WSHentAktoerIdForIdentResponse;
import no.nav.tjeneste.virksomhet.aktoer.v2.meldinger.WSHentIdentForAktoerIdRequest;
import no.nav.tjeneste.virksomhet.aktoer.v2.meldinger.WSHentIdentForAktoerIdResponse;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Slf4j
public class AktoerServiceImpl implements AktoerService {

    @Inject
    private AktoerV2 soapService;

    @Inject
    private JdbcTemplate db;

    @Inject
    private BrukerRepository brukerRepository;

    public Try<PersonId> hentPersonidFraAktoerid(AktoerId aktoerId) {
        Try<PersonId> personid = brukerRepository.retrievePersonid(aktoerId);

        if(personid.isSuccess() && personid.get() == null) {
            return hentPersonIdViaSoap(aktoerId);
        }
        return personid;
    }

    @Override
    public Try<AktoerId> hentAktoeridFraPersonid(String personid) {
        return hentSingleFraDb(
                db,
                "SELECT AKTOERID FROM AKTOERID_TO_PERSONID WHERE PERSONID = ?",
                (data) -> (String) data.get("aktoerid"),
                personid
        ).map(AktoerId::new);
    }

    @Override
    public Try<AktoerId> hentAktoeridFraFnr(Fnr fnr) {
        return Try.of(() -> soapService.hentAktoerIdForIdent(new WSHentAktoerIdForIdentRequest().withIdent(fnr.toString())))
                .map(WSHentAktoerIdForIdentResponse::getAktoerId)
                .map(AktoerId::new);
    }

    @Override
    public Try<Fnr> hentFnrFraAktoerid(AktoerId aktoerId) {
        return hentFnrViaSoap(aktoerId);
    }

    private Try<PersonId> hentPersonIdViaSoap(AktoerId aktoerId) {
        return hentFnrViaSoap(aktoerId)
                .flatMap(brukerRepository::retrievePersonidFromFnr)
                .andThen(personId -> brukerRepository.insertAktoeridToPersonidMapping(aktoerId, personId))
                .onFailure(e -> log.warn("Kunne ikke finne personId for aktoerId {}.", aktoerId));
    }

    private Try<Fnr> hentFnrViaSoap(AktoerId aktoerId) {
        WSHentIdentForAktoerIdRequest soapRequest = new WSHentIdentForAktoerIdRequest().withAktoerId(aktoerId.toString());

        return
                Try.of(
                        () -> soapService.hentIdentForAktoerId(soapRequest))
                        .map(WSHentIdentForAktoerIdResponse::getIdent)
                        .map(Fnr::new)
                        .onFailure(e -> log.warn("SOAP-Kall mot baksystem (AktoerV2) feilet for aktoerId {} | {}", aktoerId, e.getMessage())
                        );
    }

    private static <T> Try<T> hentSingleFraDb(JdbcTemplate db, String sql, Function<Map<String, Object>, T> mapper, Object... args) {
        List<Map<String, Object>> data = db.queryForList(sql, args);
        if (data.size() != 1) {
            return Try.failure(new RuntimeException("Kunne ikke hente single fra Db"));
        }
        return Try.success(mapper.apply(data.get(0)));
    }
}