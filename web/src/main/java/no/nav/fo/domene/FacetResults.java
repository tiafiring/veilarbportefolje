package no.nav.fo.domene;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class FacetResults {
    private List<Facet> facetResults;
}