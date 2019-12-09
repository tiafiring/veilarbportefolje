package no.nav.fo.veilarbportefolje.provider.rest;

import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import no.nav.common.auth.SsoToken;
import no.nav.common.auth.Subject;
import no.nav.common.auth.SubjectHandler;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.function.Supplier;

import static javax.ws.rs.core.Response.Status.OK;

@Slf4j
public class RestUtils {
    static Response createResponse(Supplier<Object> supplier) {
        return Try.ofSupplier(supplier)
                .toEither()
                .fold(
                        (throwable) -> {
                            if (throwable instanceof WebApplicationException) {
                                return ((WebApplicationException) throwable).getResponse();
                            }
                            log.warn("Exception ved restkall", throwable);
                            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
                        },
                        (entity) -> Response.status(OK).entity(entity).build()
                );
    }
}
