package no.nav.fo.domene.feed;

import lombok.Data;
import lombok.experimental.Accessors;

import java.sql.Timestamp;

@Data
@Accessors(chain = true)
public class AktivitetDataFraFeed implements Comparable<AktivitetDataFraFeed> {

    public static final String FEED_NAME = "aktiviteter";

    String aktivitetId;
    String aktorId;

    Timestamp fraDato;
    Timestamp tilDato;
    Timestamp endretDato;

    String aktivitetType;
    String status;
    boolean avtalt;

    @Override
    public int compareTo(AktivitetDataFraFeed o) {
        return endretDato.compareTo(o.endretDato);
    }
}