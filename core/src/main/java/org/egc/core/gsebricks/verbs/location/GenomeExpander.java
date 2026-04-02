package org.egc.core.gsebricks.verbs.location;

import java.util.*;

import org.egc.core.genome.Genome;
import org.egc.core.genome.location.NamedRegion;
import org.egc.core.genome.location.Region;
import org.egc.core.gsebricks.verbs.Expander;


/**
 * Expander that takes a Genome and returns all X's across the entire genome
 * by iterating over chromosomes and applying the given region expander.
 * 
 * @author tdanford
 */
public class GenomeExpander<X> implements Expander<Genome,X> {
	
	private Expander<Region,X> expander;
	
	public GenomeExpander(Expander<Region,X> exp) { 
		expander = exp;
	}

	public Iterator<X> execute(Genome a) {
		ChromRegionIterator chroms = new ChromRegionIterator(a);
		List<X> results = new ArrayList<X>();
		while (chroms.hasNext()) {
			Region r = chroms.next();
			Iterator<X> items = expander.execute(r);
			while (items.hasNext()) {
				results.add(items.next());
			}
		}
		return results.iterator();
	}
}
