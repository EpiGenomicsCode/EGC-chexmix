/*
 * Created on Sep 28, 2006
 */
package org.egc.core.gsebricks.verbs.location;

import org.egc.core.genome.Genome;
import org.egc.core.genome.location.Gene;
import org.egc.core.genome.location.Region;
import org.egc.core.gsebricks.GeneFactory;
import org.egc.core.gsebricks.RegionExpanderFactory;
import org.egc.core.gsebricks.verbs.Expander;

/**
 * @author tdanford
 * 
 * Note: Gene annotation loading from database tables has been removed.
 * This stub remains to satisfy AnnotationLoader's factory registration.
 */
public class RefGeneGeneratorFactory implements RegionExpanderFactory<Gene>, GeneFactory {
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
