package no.nav.pto.veilarbportefolje.domene;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class KafkaVedtakStatusEndring {

    public enum KafkaVedtakStatus {
        UTKAST_OPPRETTET, SENDT_TIL_BESLUTTER, SENDT_TIL_BRUKER, UTKAST_SLETTET
    }
    long id;
    String aktorId;
    KafkaVedtakStatus vedtakStatus;
    Innsatsgruppe innsatsgruppe;
    Hovedmal hovedmal;
    LocalDateTime sistRedigertTidspunkt;
    LocalDateTime statusEndretTidspunkt;
}
