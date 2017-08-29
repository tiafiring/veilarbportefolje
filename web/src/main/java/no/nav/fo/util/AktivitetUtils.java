package no.nav.fo.util;

import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import no.nav.fo.database.BrukerRepository;
import no.nav.fo.domene.AktivitetStatus;
import no.nav.fo.domene.AktoerId;
import no.nav.fo.domene.PersonId;
import no.nav.fo.domene.aktivitet.*;
import no.nav.fo.service.AktoerService;
import org.apache.solr.common.SolrInputDocument;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.*;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static no.nav.fo.domene.aktivitet.AktivitetData.aktivitetTyperList;

@Slf4j
public class AktivitetUtils {

    public static List<AktivitetBrukerOppdatering> konverterTilBrukerOppdatering(List<AktoerAktiviteter> aktoerAktiviteter, AktoerService aktoerService) {
        return aktoerAktiviteter
                .stream()
                .map(aktoerAktivitet -> {
                    AktoerId aktoerId = new AktoerId(aktoerAktivitet.getAktoerid());
                    Try<PersonId> personid = getPersonId(aktoerId, aktoerService)
                            .onFailure((e) -> log.warn("Kunne ikke hente personid for aktoerid {}", aktoerAktivitet.getAktoerid(), e));

                    return personid.isSuccess() && personid.get() != null ?
                            konverterTilBrukerOppdatering(aktoerAktivitet.getAktiviteter(), aktoerId, personid.get()) :
                            null;
                })
                .filter(Objects::nonNull)
                .collect(toList());
    }


    public static AktivitetBrukerOppdatering konverterTilBrukerOppdatering(List<AktivitetDTO> aktiviteter, AktoerId aktoerId, PersonId personId) {

        Set<AktivitetStatus> aktiveAktiviteter = lagAktivitetSet(aktiviteter, LocalDate.now(), aktoerId, personId);
        Boolean erIAvtaltIAvtaltAktivitet = erBrukerIAktivAktivitet(aktiviteter, LocalDate.now());
        Optional<AktivitetDTO> nyesteUtlopteAktivitet = Optional.ofNullable(finnNyesteUtlopteAktivAktivitet(aktiviteter, LocalDate.now()));

        return new AktivitetBrukerOppdatering(personId.toString(), aktoerId.toString())
                .setAktiviteter(aktiveAktiviteter)
                .setIAvtaltAktivitet(erIAvtaltIAvtaltAktivitet)
                .setNyesteUtlopteAktivitet(nyesteUtlopteAktivitet.map(AktivitetDTO::getTilDato).orElse(null));
    }


    public static AktivitetBrukerOppdatering hentAktivitetBrukerOppdatering(AktoerId aktoerid, AktoerService aktoerService, BrukerRepository brukerRepository) {
        PersonId personid = getPersonId(aktoerid, aktoerService)
                .onFailure((e) -> log.warn("Kunne ikke hente personid for aktoerid {}", aktoerid, e))
                .get();

        List<AktivitetDTO> aktiviteter = brukerRepository.getAktiviteterForAktoerid(aktoerid);

        return konverterTilBrukerOppdatering(aktiviteter, aktoerid, personid);
    }

    public static Boolean erBrukersAktivitetAktiv(List<String> aktivitetStatusListe) {
        return aktivitetStatusListe
                .stream()
                .filter(status -> !AktivitetFullfortStatuser.contains(status))
                .anyMatch(match -> true);
    }

    public static boolean erBrukerIAktivAktivitet(List<AktivitetDTO> aktiviteter, LocalDate today) {
        return aktiviteter
                .stream()
                .filter(AktivitetUtils::harIkkeStatusFullfort)
                .filter(aktivitet -> erAktivitetIPeriode(aktivitet, today))
                .anyMatch(match -> true);

    }

    public static boolean erAktivitetIPeriode(AktivitetDTO aktivitet, LocalDate today) {
        if (aktivitet.getTilDato() == null) {
            return true; // Aktivitet er aktiv dersom tildato ikke er satt
        }
        LocalDate tilDato = aktivitet.getTilDato().toLocalDateTime().toLocalDate();

        return today.isBefore(tilDato.plusDays(1));
    }

    public static AktivitetDTO finnNyesteUtlopteAktivAktivitet(List<AktivitetDTO> aktiviteter, LocalDate today) {
        return aktiviteter
                .stream()
                .filter(AktivitetUtils::harIkkeStatusFullfort)
                .filter(aktivitet -> Objects.nonNull(aktivitet.getTilDato()))
                .filter(aktivitet -> aktivitet.getTilDato().toLocalDateTime().toLocalDate().isBefore(today))
                .sorted(Comparator.comparing(AktivitetDTO::getTilDato))
                .findFirst()
                .orElse(null);
    }

    public static Set<AktivitetStatus> lagAktivitetSet(List<AktivitetDTO> aktiviteter, LocalDate today, AktoerId aktoerId, PersonId personId) {
        Set<AktivitetStatus> aktiveAktiviteter = new HashSet<>();

        aktivitetTyperList
                .stream()
                .map(Objects::toString)
                .forEach(aktivitetsype -> {

                    List<AktivitetDTO> aktiviteterIPeriodeMedAktivtStatus = aktiviteter
                            .stream()
                            .filter(aktivitet -> aktivitetsype.equals(aktivitet.getAktivitetType()))
                            .filter(aktivitet -> erAktivitetIPeriode(aktivitet, today))
                            .filter(AktivitetUtils::harIkkeStatusFullfort)
                            .collect(toList());

                    Timestamp datoForNesteUtlop = aktiviteterIPeriodeMedAktivtStatus
                            .stream()
                            .map(AktivitetDTO::getTilDato)
                            .sorted()
                            .findFirst()
                            .orElse(null);

                    boolean aktivitetErIkkeFullfortEllerUtlopt = !aktiviteterIPeriodeMedAktivtStatus.isEmpty();

                    aktiveAktiviteter.add(
                            AktivitetStatus.of(
                                    personId,
                                    aktoerId,
                                    aktivitetsype,
                                    aktivitetErIkkeFullfortEllerUtlopt,
                                    datoForNesteUtlop
                            )
                    );
                });

        return aktiveAktiviteter;
    }

    public static void applyAktivitetStatuser(SolrInputDocument dokument, BrukerRepository brukerRepository) {
        applyAktivitetStatuser(singletonList(dokument), brukerRepository);
    }


    public static void applyAktivitetStatuser(List<SolrInputDocument> dokumenter, BrukerRepository brukerRepository) {
        io.vavr.collection.List.ofAll(dokumenter)
                .sliding(1000, 1000)
                .forEach((dokumenterBatch) -> {
                    List<PersonId> personIds = dokumenterBatch.toJavaList().stream()
                            .map((dokument) -> new PersonId((String) dokument.get("person_id").getValue())).collect(toList());

                    Map<PersonId, Set<AktivitetStatus>> aktivitetStatuser = brukerRepository.getAktivitetstatusForBrukere(personIds);

                    dokumenterBatch.forEach((dokument) -> {
                        PersonId personId = new PersonId((String) dokument.get("person_id").getValue());
                        applyAktivitetstatusToDocument(dokument, aktivitetStatuser.get(personId));
                    });
                });
    }

    private static void applyAktivitetstatusToDocument(SolrInputDocument document, Set<AktivitetStatus> aktivitetStatuser) {
        if (aktivitetStatuser == null) {
            return;
        }
        List<String> aktiveAktiviteter = aktivitetStatuser
                .stream()
                .filter(AktivitetStatus::isAktiv)
                .map(AktivitetStatus::getAktivitetType)
                .collect(toList());

        Map<String, String> aktivitTilUtlopsdato = aktivitetStatuser
                .stream()
                .filter(AktivitetStatus::isAktiv)
                .filter(aktivitetStatus -> Objects.nonNull(aktivitetStatus.getNesteUtlop()))
                .collect(toMap(AktivitetStatus::getAktivitetType,
                        aktivitetStatus -> DateUtils.iso8601FromTimestamp(aktivitetStatus.getNesteUtlop()),
                        (v1, v2) -> v2));

        String aktiviteterUtlopsdatoJSON = new JSONObject(aktivitTilUtlopsdato).toString();

        document.addField("aktiviteter", aktiveAktiviteter);
        document.addField("aktiviteter_utlopsdato_json", aktiviteterUtlopsdatoJSON);
    }


    public static Object applyTiltak(List<SolrInputDocument> dokumenter, BrukerRepository brukerRepository) {
        dokumenter.stream().forEach(document -> {
            String personid = (String) document.get("person_id").getValue();
            List<String> tiltak = brukerRepository.getTiltak(personid);
            if (!tiltak.isEmpty()) {
                document.addField("tiltak", tiltak);
            }
        });
        return null;
    }

    static Try<PersonId> getPersonId(AktoerId aktoerid, AktoerService aktoerService) {
        return aktoerService
                .hentPersonidFraAktoerid(aktoerid);
    }

    static boolean harIkkeStatusFullfort(AktivitetDTO aktivitetDTO) {
        return !AktivitetFullfortStatuser.contains(aktivitetDTO.getStatus());
    }
}