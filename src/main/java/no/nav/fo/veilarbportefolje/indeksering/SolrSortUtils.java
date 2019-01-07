package no.nav.fo.veilarbportefolje.indeksering;

import no.nav.fo.veilarbportefolje.domene.AktivitetFiltervalg;
import no.nav.fo.veilarbportefolje.domene.Filtervalg;
import no.nav.fo.veilarbportefolje.domene.aktivitet.AktivitetData;
import org.apache.solr.client.solrj.SolrQuery;

import java.util.*;
import java.util.stream.Collectors;

import static no.nav.fo.veilarbportefolje.provider.rest.ValideringsRegler.sortFields;
import static no.nav.fo.veilarbportefolje.util.AktivitetUtils.addPrefixForAktivitetUtlopsdato;

public class SolrSortUtils {


    public static SolrQuery addSort(SolrQuery solrQuery, boolean minoversikt, Optional<String> maybeVeilederMedTilgangQuery, String sortOrder, String sortField, Filtervalg filtervalg) {

        if(minoversikt){
            solrQuery.addSort("ny_for_veileder", SolrQuery.ORDER.desc);
        } else {
            maybeVeilederMedTilgangQuery.ifPresent(veilederMedTilgangQuery -> solrQuery.addSort(veilederMedTilgangQuery, SolrQuery.ORDER.asc));
        }

        SolrQuery.ORDER order = "ascending".equals(sortOrder) ? SolrQuery.ORDER.asc : SolrQuery.ORDER.desc;
        if("ikke_satt".equals(sortField)) {
            return solrQuery.addSort("person_id", SolrQuery.ORDER.asc);
        }
        if("valgteaktiviteter".equals(sortField)) {
            return addAktiviteterSort(solrQuery, order, filtervalg.getAktiviteter());
        }
        if("iavtaltaktivitet".equals(sortField)) {
            return addAktiviteterSort(solrQuery, order, mapMedAlleAktivitetstatuserSattTilJA());
        }
        if("fodselsnummer".equals(sortField)) {
            return solrQuery.addSort("fnr", order);
        }
        if("utlopteaktiviteter".equals(sortField)) {
            return solrQuery.addSort("nyesteutlopteaktivitet", order);
        }
        if("etternavn".equals(sortField)) {
            solrQuery.addSort("etternavn", order);
            return solrQuery.addSort("fornavn", order);
        }
        if("arbeidslistefrist".equals(sortField)) {
            return solrQuery.addSort("arbeidsliste_frist", order);
        }
        if("aaprettighetsperiode".equals(sortField)) {
            return solrQuery.addSort("max(aapmaxtiduke, aapunntakukerigjen)", order);
        }
        if(sortFields.contains(sortField)) {
            return solrQuery.addSort(sortField, order);
        }
        return solrQuery;
    }

    public static Map<String, AktivitetFiltervalg> mapMedAlleAktivitetstatuserSattTilJA() {
        Map<String, AktivitetFiltervalg> map = new HashMap<>();
        AktivitetData.aktivitetTyperList.forEach(type -> map.put(type.name(), AktivitetFiltervalg.JA));
        return map;
    }

    public static SolrQuery addAktiviteterSort(SolrQuery solrQuery, SolrQuery.ORDER sortOrder, Map<String, AktivitetFiltervalg> aktiviteter) {
        List<String> aktiviteterSortFields = aktiveAktiviteter(aktiviteter);
        String sortString = getAktiviteterSortString(aktiviteterSortFields);
        solrQuery.addSort(sortString, sortOrder);
        return solrQuery;
    }

    public static List<String> aktiveAktiviteter(Map<String, AktivitetFiltervalg> aktiviteter) {
        List<String> sortFields = new ArrayList<>();
        aktiviteter.forEach((key, value) -> {
            if(AktivitetFiltervalg.JA.equals(value)) {
                sortFields.add(addPrefixForAktivitetUtlopsdato(key));
            }
        });
        return sortFields;
    }

    private static String getAktiviteterSortString(List<String> felt) {
        if(felt.size() == 1) {
            return felt.get(0).toLowerCase();
        }
        return ("min(" + felt.stream().collect(Collectors.joining(",")) + ")").toLowerCase();
    }

    public static SolrQuery addPaging(SolrQuery solrQuery, Integer fra, Integer antall) {
        if(Objects.nonNull(fra) && Objects.nonNull(antall)) {
            solrQuery.setStart(fra);
            solrQuery.setRows(antall);
        }
        return solrQuery;
    }
}