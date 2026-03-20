/*
 * Created on Sep 28, 2006
 */
package org.egc.core.gsebricks;

import org.egc.core.genome.Genome;
import org.egc.core.genome.location.Gene;
import org.egc.core.genome.location.Region;
import org.egc.core.gsebricks.verbs.Expander;

/**
 * @author tdanford
 *
 * <code>GeneFactory</code> returns an Expander than maps Regions to Genes.  The purpose
 * of the Factory is to return an appropriate expander for a given Genome.
 */
public interface GeneFactory {
    public Expander<Region,Gene> getExpander(Genome g);
}
