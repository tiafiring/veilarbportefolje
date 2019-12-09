package no.nav.fo.veilarbportefolje.config;

import lombok.extern.slf4j.Slf4j;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.Fallback;
import net.jodah.failsafe.RetryPolicy;
import net.jodah.failsafe.Timeout;
import no.nav.brukerdialog.security.oidc.SystemUserTokenProvider;
import no.nav.fo.veilarbportefolje.FailSafeConfig;
import no.nav.sbl.rest.RestUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import java.util.function.Function;

import static java.time.Duration.ofSeconds;

@Slf4j
@Configuration
public class ClientConfig {

    private static FailSafeConfig defaultFailsafeConfig = FailSafeConfig.builder()
            .maxRetries(3)
            .retryDelay(ofSeconds(8))
            .timeout(ofSeconds(30))
            .build();

    public static <T> T usingFailSafeClient(FailSafeConfig config, Function<Client, T> function) {

        RetryPolicy<T> retryPolicy = new RetryPolicy<T>()
                .withDelay(config.getRetryDelay())
                .withMaxRetries(config.getMaxRetries())
                .onRetry(retry -> log.info("Retrying...", retry.getLastFailure()))
                .onFailure(failure -> log.error("Call failed", failure.getFailure()))
                .onSuccess(success -> log.info("Call succeeded after {} attempt(s)", success.getAttemptCount()));

        Timeout<T> timeout = Timeout.of(config.getTimeout());

        Fallback<T> fallbackPolicy = Fallback.of(() -> null);

        return Failsafe
                .with(retryPolicy, fallbackPolicy, timeout)
                .get(() -> RestUtils.withClient(function));
    }

    public static <T> T usingFailSafeClient(Function<Client, T> function) {
        return usingFailSafeClient(getDefaultFailsafeConfig(), function);
    }

    public static FailSafeConfig getDefaultFailsafeConfig() {
        return defaultFailsafeConfig;
    }

    public static void setDefaultFailsafeConfig(FailSafeConfig defaultFailsafeConfig) {
        ClientConfig.defaultFailsafeConfig = defaultFailsafeConfig;
    }

    @Bean
    public Client client() {
        Client client = RestUtils.createClient();
        client.register(new SystemUserOidcTokenProviderFilter());
        return client;
    }

    private static class SystemUserOidcTokenProviderFilter implements ClientRequestFilter {
        private SystemUserTokenProvider systemUserTokenProvider = new SystemUserTokenProvider();

        @Override
        public void filter(ClientRequestContext clientRequestContext) {
            clientRequestContext.getHeaders().putSingle("Authorization", "Bearer " + systemUserTokenProvider.getToken());
        }
    }

}
