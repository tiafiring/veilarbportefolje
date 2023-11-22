package no.nav.pto.veilarbportefolje.huskelapp;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import no.nav.common.types.identer.EnhetId;
import no.nav.common.types.identer.Fnr;
import no.nav.pto.veilarbportefolje.domene.value.VeilederId;
import no.nav.pto.veilarbportefolje.huskelapp.controller.dto.HuskelappInputDto;
import no.nav.pto.veilarbportefolje.huskelapp.controller.dto.HuskelappOutputDto;
import no.nav.pto.veilarbportefolje.huskelapp.domain.HuskelappStatus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static no.nav.pto.veilarbportefolje.database.PostgresTable.HUSKELAPP.*;
import static no.nav.pto.veilarbportefolje.util.DateUtils.toLocalDate;

@RequiredArgsConstructor
public class HuskelappRepository {
    private final JdbcTemplate db;
    @Qualifier("PostgresJdbcReadOnly")
    private final JdbcTemplate dbReadOnly;

    public UUID opprettHuskelapp(HuskelappInputDto inputDto, VeilederId veilederId) {
        UUID huskelappId = UUID.randomUUID();
        UUID endringsId = UUID.randomUUID();
        String sql = """
                INSERT INTO HUSKELAPP (
                    ENDRINGS_ID,
                    HUSKELAPP_ID,
                    FNR,
                    ENHET_ID,
                    ENDRET_AV_VEILEDER,
                    ENDRET_DATO,
                    FRIST,
                    KOMMENTAR,
                    STATUS
                )
                VALUES (
                    ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?, ?, ?
                )
                """;
        int rowsUpdated = db.update(sql, endringsId, huskelappId, inputDto.brukerFnr().get(), inputDto.enhetId(), veilederId.getValue(), inputDto.frist(), inputDto.kommentar(), HuskelappStatus.AKTIV);
        if(rowsUpdated > 0) {
            return huskelappId;
        } else {
            throw new RuntimeException("Kunne ikke opprette huskelapp");
        }
    }

    public void oppdaterHuskelapp()

    public List<HuskelappOutputDto> hentHuskelapp(EnhetId enhetId, VeilederId veilederId) {
        return dbReadOnly.queryForList("""
                                SELECT hl.* FROM HUSKELAPP hl
                                INNER JOIN aktive_identer ai on ai.fnr = hl.fnr
                                INNER JOIN oppfolging_data o ON ai.aktorid = o.aktoerid
                                INNER JOIN oppfolgingsbruker_arena_v2 ob on ai.fnr = ob.fodselsnr
                                WHERE ob.nav_kontor = ?
                                AND o.veilederid = ?""",
                        enhetId.get(),
                        veilederId.getValue()
                )
                .stream()
                .map(HuskelappRepository::huskelappOutputListMapper)
                .toList();

    }

    public Optional<HuskelappOutputDto> hentHuskelapp(Fnr brukerFnr) {
        String sql = String.format("SELECT * FROM %s WHERE %s=? ", TABLE_NAME, FNR);
        return dbReadOnly.queryForList(sql, brukerFnr.get()).stream().map(HuskelappRepository::huskelappOutputListMapper).findFirst();
    }

    public Optional<HuskelappOutputDto> hentHuskelapp(UUID huskelappId) {
        String sql = String.format("SELECT * FROM %s WHERE %s=? ", TABLE_NAME, HUSKELAPP_ID);
        return dbReadOnly.queryForList(sql, huskelappId).stream().map(HuskelappRepository::huskelappOutputListMapper).findFirst();
    }

    public boolean slettHuskelapperpaa(UUID huskelappId) {
        String sql = String.format("DELETE FROM %s WHERE %s=? ", TABLE_NAME, HUSKELAPP_ID);
        int rowsUpdated = db.update(sql, huskelappId);
        return rowsUpdated > 0;
    }

    public void oppdatereStatus(UUID huskelappId, HuskelappStatus status) {
        String sql = String.format(
                "UPDATE %s SET %s = ? WHERE %s = ?",
                TABLE_NAME, STATUS, HUSKELAPP_ID
        );
        db.update(sql, status, huskelappId);
    }

    public void oppdatereArkivertDato(UUID huskelappId) {
       // String sql = String.format(
       //         "UPDATE %s SET %s = CURRENT_TIMESTAMP WHERE %s = ?",
       //         TABLE_NAME, ARKIVERT_DATO, ID
       // );
       // db.update(sql, huskelappId);
    }

    @SneakyThrows
    private static HuskelappOutputDto huskelappOutputListMapper(Map<String, Object> rs) {
        return new HuskelappOutputDto(
                (String)rs.get(HUSKELAPP_ID),
                Fnr.of((String)rs.get(FNR)),
                toLocalDate((Timestamp)rs.get(ENDRET_DATO)),
                (String)rs.get(KOMMENTAR));
    }
}
