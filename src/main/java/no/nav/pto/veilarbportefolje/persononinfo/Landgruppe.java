package no.nav.pto.veilarbportefolje.persononinfo;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Landgruppe {
    private static Map<String, String> landGruppe0 = Stream.of(new String[][]{
            {"NOR", "Norge"},
            {"SJM", "Svalbard og Jan Mayen"}
    }).collect(Collectors.toMap(data -> data[0], data -> data[1]));

    private static Map<String, String> landGruppe1 = Stream.of(new String[][]{
            {"AND", "Andorra"},
            {"AUS", "Australia"},
            {"AUT", "Østerrike"},
            {"BEL", "Belgia"},
            {"CAN", "Canada"},
            {"CHE", "Sveits"},
            {"CYP", "Kypros"},
            {"DDR", "Den tyske demokratiske republikken"},
            {"DEU", "Tyskland"},
            {"DNK", "Danmark"},
            {"ESP", "Spania"},
            {"FIN", "Finland"},
            {"FRA", "Frankrike"},
            {"GBR", "Storbritannia"},
            {"GIB", "Gibraltar"},
            {"GRC", "Hellas"},
            {"GRL", "Grønland"},
            {"IRL", "Irland"},
            {"ISL", "Island"},
            {"ITA", "Italia"},
            {"LIE", "Liechtenstein"},
            {"LUX", "Luxemburg"},
            {"MCO", "Monaco"},
            {"MLT", "Malta"},
            {"NLD", "Nederland"},
            {"NZL", "New Zealand"},
            {"PRT", "Portugal"},
            {"ROM", "Romania"},
            {"SMR", "San Marino"},
            {"SWE", "Sverige"},
            {"USA", "U S A"},
            {"VAT", "Vatikanstaten"}
    }).collect(Collectors.toMap(data -> data[0], data -> data[1]));

    private static Map<String, String> landGruppe2 = Stream.of(new String[][]{
            {"BGR", "Bulgaria"},
            {"CSK", "Tsjekkoslovakia"},
            {"CZE", "Den Tsjekkiske Rep."},
            {"EST", "Estland"},
            {"HRV", "Kroatia"},
            {"HUN", "Ungarn"},
            {"LTU", "Litauen"},
            {"LVA", "Latvia"},
            {"POL", "Polen"},
            {"ROU", "Romania"},
            {"SVK", "Slovakia"},
            {"SVN", "Slovenia"}
    }).collect(Collectors.toMap(data -> data[0], data -> data[1]));

    private static Map<String, String> landGruppe3 = Stream.of(new String[][]{
            {"349", "Spanske Omr.Afrika"},
            {"546", "Sikkim"},
            {"556", "Yemen"},
            {"669", "Panamakanalsonen"},
            {"ABW", "Aruba"},
            {"AFG", "Afghanistan"},
            {"AGO", "Angola"},
            {"AIA", "Anguilla"},
            {"ALA", "Åland"},
            {"ALB", "Albania"},
            {"ANT", "Antillene(NL)"},
            {"ARE", "De Forente Arabiske Emirater"},
            {"ARG", "Argentina"},
            {"ARM", "Armenia"},
            {"ASM", "Amerikansk Samoa"},
            {"ATA", "Antarktis"},
            {"ATF", "Fransk sørlig og antarktisk territorium"},
            {"ATG", "Antigua og Barbuda"},
            {"AZE", "Aserbajdsjan"},
            {"BDI", "Burundi"},
            {"BEN", "Benin"},
            {"BES", "Bonaire, St.Eustatius og Saba."},
            {"BFA", "Burkina Faso"},
            {"BGD", "Bangladesh"},
            {"BHR", "Bahrain"},
            {"BHS", "Bahamas"},
            {"BIH", "Bosnia - Hercegovina"},
            {"BLM", "Saint Barthélemy"},
            {"BLR", "Hviterussland"},
            {"BLZ", "Belize"},
            {"BMU", "Bermuda"},
            {"BOL", "Bolivia"},
            {"BRA", "Brasil"},
            {"BRB", "Barbados"},
            {"BRN", "Brunei Darussalam"},
            {"BTN", "Bhutan"},
            {"BVT", "Bovetøya"},
            {"BWA", "Botswana"},
            {"CAF", "Sentral - Afrikanske Republikk"},
            {"CCK", "Kokosøyene(Keelingøyene)"},
            {"CHL", "Chile"},
            {"CHN", "Kina"},
            {"CIV", "Elfenbenskysten"},
            {"CMR", "Kamerun"},
            {"COD", "Kongo"},
            {"COG", "Kongo, Brazzaville"},
            {"COK", "Cookøyene"},
            {"COL", "Colombia"},
            {"COM", "Komorene"},
            {"CPV", "Kapp Verde"},
            {"CRI", "Costa Rica"},
            {"CUB", "Cuba"},
            {"CUW", "Curaçao"},
            {"CXR", "Christmasøya"},
            {"CYM", "Caymanøyene"},
            {"DJI", "Djibouti"},
            {"DMA", "Dominica"},
            {"DOM", "Den Dominikanske Republikk"},
            {"DPO", "Det Palestinske Området"},
            {"DZA", "Algerie"},
            {"ECU", "Ecuador"},
            {"EGY", "Egypt"},
            {"ERI", "Eritrea"},
            {"ESC", "Kanariøyene"},
            {"ESH", "Vest - Sahara"},
            {"ETH", "Etiopia"},
            {"FJI", "Fiji"},
            {"FLK", "Falklandsøyene(Malvinas)"},
            {"FRO", "Færøyene"},
            {"FSM", "Mikronesia føderasjonen"},
            {"GAB", "Gabon"},
            {"GEO", "Georgia"},
            {"GGY", "Guernsey"},
            {"GHA", "Ghana"},
            {"GIN", "Guinea"},
            {"GLP", "Guadeloupe"},
            {"GMB", "Gambia"},
            {"GNB", "Guinea - Bissau"},
            {"GNQ", "Ekvatorial - Guinea"},
            {"GRD", "Grenada"},
            {"GTM", "Guatemala"},
            {"GUF", "Fransk Guyana"},
            {"GUM", "Guam"},
            {"GUY", "Guyana"},
            {"HKG", "Hong Kong"},
            {"HMD", "Herald - og McDonaldøyene"},
            {"HND", "Honduras"},
            {"HRX", "Kroatia"},
            {"HTI", "Haiti"},
            {"IDN", "Indonesia"},
            {"IMN", "Isle of Man"},
            {"IND", "India"},
            {"IOT", "Det britiske terriotoriet i Indiahavet"},
            {"IRN", "Iran"},
            {"IRQ", "Irak"},
            {"ISR", "Israel"},
            {"JAM", "Jamaica"},
            {"JEY", "Jersey"},
            {"JOR", "Jordan"},
            {"JPN", "Japan"},
            {"KAZ", "Kasakhstan"},
            {"KEN", "Kenya"},
            {"KGZ", "Kirgisistan"},
            {"KHM", "Kambodsja"},
            {"KIR", "Kiribati"},
            {"KNA", "Saint Kitts og Nevis"},
            {"KOR", "Sør - Korea"},
            {"KWT", "Kuwait"},
            {"LAO", "Laos"},
            {"LBN", "Libanon"},
            {"LBR", "Liberia"},
            {"LBY", "Libya"},
            {"LCA", "Saint Lucia"},
            {"LKA", "Sri Lanka"},
            {"LSO", "Lesotho"},
            {"MAC", "Macao"},
            {"MAF", "Saint Martin"},
            {"MAR", "Marokko"},
            {"MDA", "Moldova"},
            {"MDG", "Madagaskar"},
            {"MDV", "Maldivene"},
            {"MEX", "Mexico"},
            {"MHL", "Marshalløyene"},
            {"MKD", "Makedonia"},
            {"MLI", "Mali"},
            {"MMR", "Myanmar el.Burma"},
            {"MNE", "Montenegro"},
            {"MNG", "Mongolia"},
            {"MNP", "Nordre Marianene"},
            {"MOZ", "Mosambik"},
            {"MRT", "Mauritania"},
            {"MSR", "Montserrat"},
            {"MTQ", "Martinique"},
            {"MUS", "Mauritius"},
            {"MWI", "Malawi"},
            {"MYS", "Malaysia"},
            {"MYT", "Mayotte"},
            {"NAM", "Namibia"},
            {"NCL", "Ny Caledonia"},
            {"NER", "Niger"},
            {"NFK", "Norfolkøya"},
            {"NGA", "Nigeria"},
            {"NIC", "Nicaragua"},
            {"NIU", "Niue"},
            {"NPL", "Nepal"},
            {"NRU", "Nauru"},
            {"NTZ", "Nøytralsone"},
            {"OMN", "Oman"},
            {"PAK", "Pakistan"},
            {"PAN", "Panama"},
            {"PCN", "Pitcairn"},
            {"PER", "Peru"},
            {"PHL", "Filippinene"},
            {"PLW", "Palau"},
            {"PNG", "Papua - Ny Guinea"},
            {"PRI", "Puerto Rico"},
            {"PRK", "Nord-Korea"},
            {"PRY", "Paraguay"},
            {"PSE", "Det palestinske området"},
            {"PYF", "Fransk Polynesia"},
            {"QAT", "Quatar"},
            {"REU", "Reunion"},
            {"RUS", "Russland"},
            {"RWA", "Rwanda"},
            {"SAU", "Saudi-Arabia"},
            {"SCG", "Montenegro"},
            {"SDN", "Sudan"},
            {"SEN", "Senegal"},
            {"SGP", "Singapore"},
            {"SGS", "Sør- Georgia og De Søndre Sandwichøyene"},
            {"SHN", "Sankt Helena"},
            {"SLB", "Salomonøyene"},
            {"SLE", "Sierra Leone"},
            {"SLV", "El Salvador"},
            {"SOM", "Somalia"},
            {"SPM", "St.Pierre og Miquelon"},
            {"SRB", "Serbia"},
            {"SSD", "Sør-Sudan"},
            {"STP", "São Tomé og Príncipe"},
            {"SUN", "Sovjetunionen"},
            {"SUR", "Surinam"},
            {"SWZ", "Swaziland"},
            {"SXM", "Sint Maarten"},
            {"SYC", "Seychellene"},
            {"SYR", "Syria"},
            {"TCA", "Turks og Caicosøy"},
            {"TCD", "Tsjad"},
            {"TGO", "Togo"},
            {"THA", "Thailand"},
            {"TJK", "Tadsjikistan"},
            {"TKL", "Tokelau"},
            {"TKM", "Turkmenistan"},
            {"TLS", "Øst-Timor"},
            {"TON", "Tonga"},
            {"TTO", "Trinidad og Tobago"},
            {"TUN", "Tunisia"},
            {"TUR", "Tyrkia"},
            {"TUV", "Tuvalu"},
            {"TWN", "Taiwan"},
            {"TZA", "Tanzania"},
            {"UGA", "Uganda"},
            {"Ukjent", "Ukjent"},
            {"UKR", "Ukraina"},
            {"UMI", "U.S.A mindre u.øyer"},
            {"URY", "Uruguay"},
            {"UZB", "Usbekistan"},
            {"VCT", "Saint Vincent og Grenadine"},
            {"VEN", "Venezuela"},
            {"VGB", "Jomfruøyene(GB)"},
            {"VIR", "Jomfruøyene(US)"},
            {"VNM", "Vietnam"},
            {"VUT", "Vanuatu"},
            {"WAK", "Wake Island"},
            {"WLF", "Wallis og Futuna"},
            {"WSM", "Samoa"},
            {"XXK", "Kosovo"},
            {"XXX", "Ukjent"},
            {"YEM", "Jemen"},
            {"YUG", "Jugoslavia"},
            {"ZAF", "Sør-Afrika"},
            {"ZMB", "Zambia"},
            {"ZWE", "Zimbabwe"}
    }).collect(Collectors.toMap(data -> data[0], data -> data[1]));

    public static String getLandgruppe(String countryCode) {
        if (countryCode == null || countryCode.isEmpty()) {
            return null;
        }

        String ucCountryCode = countryCode.toUpperCase();
        if (landGruppe0.containsKey(ucCountryCode)) {
            return "0";
        } else if (landGruppe1.containsKey(ucCountryCode)) {
            return "1";
        } else if (landGruppe2.containsKey(ucCountryCode)) {
            return "2";
        } else if (landGruppe3.containsKey(ucCountryCode)) {
            return "3";
        }
        return "Optional.empty()";
    }

    public static String getLandFulltNavn(String countryCode) {
        if (countryCode == null || countryCode.isEmpty()) {
            return "";
        }

        String ucCountryCode = countryCode.toUpperCase();
        if (landGruppe0.containsKey(ucCountryCode)) {
            return landGruppe0.get(ucCountryCode);
        } else if (landGruppe1.containsKey(ucCountryCode)) {
            return landGruppe1.get(ucCountryCode);
        } else if (landGruppe2.containsKey(ucCountryCode)) {
            return landGruppe2.get(ucCountryCode);
        } else if (landGruppe3.containsKey(ucCountryCode)) {
            return landGruppe3.get(ucCountryCode);
        }
        return "";
    }

}
