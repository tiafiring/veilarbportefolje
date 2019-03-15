package no.nav.fo.veilarbportefolje.indeksering;

import lombok.SneakyThrows;
import no.nav.apiapp.selftest.Helsesjekk;
import no.nav.apiapp.selftest.HelsesjekkMetadata;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequest;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

import static no.nav.fo.veilarbportefolje.indeksering.ElasticUtils.getAlias;
import static no.nav.fo.veilarbportefolje.indeksering.ElasticUtils.getElasticHostname;
import static org.elasticsearch.cluster.health.ClusterHealthStatus.GREEN;

@Component
public class ElasticSelftest implements Helsesjekk {

    RestHighLevelClient client;

    @Inject
    public ElasticSelftest(RestHighLevelClient client) {
        this.client = client;
    }

    @Override
    @SneakyThrows
    public void helsesjekk() {
        ClusterHealthResponse health = client.cluster().health(new ClusterHealthRequest(), RequestOptions.DEFAULT);
        if (health.getStatus() != GREEN) {
            throw new RuntimeException(health.toString());
        }
    }

    @Override
    public HelsesjekkMetadata getMetadata() {
        return new HelsesjekkMetadata(
                "elasticsearch helsesjekk",
                String.format("http://%s/%s", getElasticHostname(), getAlias()),
                "Sjekker helsestatus til Elasticsearch-clusteret",
                true
        );
    }
}