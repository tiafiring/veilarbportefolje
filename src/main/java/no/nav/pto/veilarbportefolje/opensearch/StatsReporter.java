package no.nav.pto.veilarbportefolje.opensearch;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.client.OpenSearchClient;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class StatsReporter implements MeterBinder {
    @Qualifier("PostgresNamedJdbcReadOnly")
    private final NamedParameterJdbcTemplate namedDb;

    private final RestHighLevelClient restHighLevelClient;

    @Override
    public void bindTo(@NonNull MeterRegistry meterRegistry) {
        Gauge.builder("veilarbportefolje.hovedindeksering.indekserer_aktivitet_endringer.last_run", this::indeksererAktivitetEndringerLastRun)
                .register(meterRegistry);
        Gauge.builder("veilarbportefolje.hovedindeksering.indekserer_aktivitet_endringer.duration", this::indeksererAktivitetEndringerDuration)
                .register(meterRegistry);

        Gauge.builder("veilarbportefolje.hovedindeksering.deaktiver_utgatte_utdannings_aktivteter.last_run", this::deaktiverUtgatteUtdanningsAktivteterLastRun)
                .register(meterRegistry);
        Gauge.builder("veilarbportefolje.hovedindeksering.deaktiver_utgatte_utdannings_aktivteter.duration", this::deaktiverUtgatteUtdanningsAktivteterDuration)
                .register(meterRegistry);

        Gauge.builder("veilarbportefolje.hovedindeksering.indekserer_ytelse_endringer.last_run", this::indeksererYtelseEndringerLastRun)
                .register(meterRegistry);
        Gauge.builder("veilarbportefolje.hovedindeksering.indekserer_ytelse_endringer.duration", this::indeksererYtelseEndringerDuration)
                .register(meterRegistry);

        Gauge.builder("veilarbportefolje.opensearch.difference_in_versions", this::compareOpensearchVersions)
                .register(meterRegistry);
    }

    private Long indeksererAktivitetEndringerLastRun() {
        String sql = "SELECT last_success FROM SCHEDULED_TASKS WHERE task_name = :taskName::varchar";
        Timestamp sisteKjorte = Optional.ofNullable(namedDb.queryForObject(sql, new MapSqlParameterSource("taskName", "indekserer_aktivitet_endringer"), Timestamp.class)).orElseThrow(() -> new IllegalStateException("Scheduled task failed to run lately"));

        return sisteKjorte.toInstant().toEpochMilli();
    }

    private Long indeksererAktivitetEndringerDuration() {
        String sql = "SELECT execution_time FROM SCHEDULED_TASKS WHERE task_name = :taskName::varchar";
        Timestamp sisteDuration = Optional.ofNullable(namedDb.queryForObject(sql, new MapSqlParameterSource("taskName", "indekserer_aktivitet_endringer"), Timestamp.class)).orElseThrow(() -> new IllegalStateException("Scheduled task failed to run lately"));

        return sisteDuration.toInstant().toEpochMilli();
    }

    private Long deaktiverUtgatteUtdanningsAktivteterLastRun() {
        String sql = "SELECT last_success FROM SCHEDULED_TASKS WHERE task_name = :taskName::varchar";
        Timestamp sisteKjorte = Optional.ofNullable(namedDb.queryForObject(sql, new MapSqlParameterSource("taskName", "deaktiver_utgatte_utdannings_aktivteter"), Timestamp.class)).orElseThrow(() -> new IllegalStateException("Scheduled task failed to run lately"));

        return sisteKjorte.toInstant().toEpochMilli();
    }

    private Long deaktiverUtgatteUtdanningsAktivteterDuration() {
        String sql = "SELECT execution_time FROM SCHEDULED_TASKS WHERE task_name = :taskName::varchar";
        Timestamp sisteDuration = Optional.ofNullable(namedDb.queryForObject(sql, new MapSqlParameterSource("taskName", "deaktiver_utgatte_utdannings_aktivteter"), Timestamp.class)).orElseThrow(() -> new IllegalStateException("Scheduled task failed to run lately"));

        return sisteDuration.toInstant().toEpochMilli();
    }

    private Long indeksererYtelseEndringerLastRun() {
        String sql = "SELECT last_success FROM SCHEDULED_TASKS WHERE task_name = :taskName::varchar";
        Timestamp sisteKjorte = Optional.ofNullable(namedDb.queryForObject(sql, new MapSqlParameterSource("taskName", "indekserer_aktivitet_endringer"), Timestamp.class)).orElseThrow(() -> new IllegalStateException("Scheduled task failed to run lately"));

        return sisteKjorte.toInstant().toEpochMilli();
    }

    private Long indeksererYtelseEndringerDuration() {
        String sql = "SELECT execution_time FROM SCHEDULED_TASKS WHERE task_name = :taskName::varchar";
        Timestamp sisteDuration = Optional.ofNullable(namedDb.queryForObject(sql, new MapSqlParameterSource("taskName", "indekserer_aktivitet_endringer"), Timestamp.class)).orElseThrow(() -> new IllegalStateException("Scheduled task failed to run lately"));

        return sisteDuration.toInstant().toEpochMilli();
    }

    private Integer compareOpensearchVersions() {
        try {
            String serverVersion = restHighLevelClient.info(RequestOptions.DEFAULT).getVersion().getNumber();
            String libraryVersion = OpenSearchClient.class.getPackage().getImplementationVersion();

            log.info(String.format("Opensearch version: %s, opensearch lib version: %s", serverVersion, libraryVersion));

            if (serverVersion.equals(libraryVersion)) {
                return 1;
            }
            return 0;

        } catch (Exception e) {
            return 0;
        }

    }
}
