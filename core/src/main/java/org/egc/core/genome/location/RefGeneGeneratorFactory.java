package org.egc.core.genome.location;

import org.egc.core.genome.Genome;
import org.egc.core.genome.location.Gene;
import org.egc.core.genome.location.Region;
import org.egc.core.gseutils.Expander;

/**
 * Stub: Gene annotation loading from database tables has been removed.
 * This class remains to satisfy AnnotationLoader's factory registration.
 */
public class RefGeneGeneratorFactory {
    private String type;

    public RefGeneGeneratorFactory() {
    }

    public void setType(String t) {type = t;}
    public String getType() {return type;}
    public String getProduct() {return "Gene";}
    public Expander<Region, Gene> getExpander(Genome g) {
        throw new UnsupportedOperationException(
            "Database-backed gene annotation loading has been removed.");
    }

    public Expander<Region, Gene> getExpander(Genome g, String type) {
        throw new UnsupportedOperationException(
            "Database-backed gene annotation loading has been removed.");
    }
}
