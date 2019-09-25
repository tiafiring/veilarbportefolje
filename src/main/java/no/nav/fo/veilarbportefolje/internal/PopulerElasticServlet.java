package no.nav.fo.veilarbportefolje.internal;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import no.nav.fo.veilarbportefolje.indeksering.ElasticIndexer;
import org.slf4j.MDC;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static no.nav.common.utils.IdUtils.generateId;

@Slf4j
public class PopulerElasticServlet extends HttpServlet {

    private ElasticIndexer elasticIndexer;

    private static String MDC_JOB_ID = "jobId";

    public PopulerElasticServlet(ElasticIndexer elasticIndexer) {
        this.elasticIndexer = elasticIndexer;
    }

    @Override
    @SneakyThrows
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (AuthorizationUtils.isBasicAuthAuthorized(req)) {
            String jobId = generateId();
            log.info("Running job with jobId {}", jobId);

            resp.getWriter().write(String.format("Hovedindeksering i ElasticSearch startet med jobId: %s", jobId));
            resp.setStatus(200);

            MDC.put(MDC_JOB_ID, jobId);
            elasticIndexer.startIndeksering();
            MDC.remove(MDC_JOB_ID);

        } else {
            AuthorizationUtils.writeUnauthorized(resp);
        }
    }
}
