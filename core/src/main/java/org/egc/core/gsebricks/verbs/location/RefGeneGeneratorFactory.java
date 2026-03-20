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
 */
public class RefGeneGeneratorFactory implements RegionExpanderFactory<Gene>, GeneFactory {
    private String type;

    public RefGeneGeneratorFactory() {
    }

    public void setType(String t) {type = t;}
    public String getType() {return type;}
    public String getProduct() {return "Gene";}
    public Expander<Region, Gene> getExpander(Genome g) {
        return getExpander(g,type);
    }

    public Expander<Region, Gene> getExpander(Genome g, String type) {
        if (type == null) {
            return new RefGeneGenerator(g);
        } else {
            RefGeneGenerator gg = new RefGeneGenerator(g, type);
            return gg;
        }
    }
}
