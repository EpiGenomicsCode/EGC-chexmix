package org.egc.core.gsebricks.verbs.location;

import java.util.*;

import org.egc.core.genome.Genome;
import org.egc.core.genome.location.NamedRegion;
import org.egc.core.genome.location.Region;
import org.egc.core.gsebricks.verbs.CastingMapper;
import org.egc.core.gsebricks.verbs.Expander;
import org.egc.core.gsebricks.verbs.ExpanderIterator;
import org.egc.core.gsebricks.verbs.Mapper;
import org.egc.core.gsebricks.verbs.MapperIterator;


/**
 * This encapsulates the little pattern that we always have to write when we want
 * to get all of some X in an *entire* genome.
 * 
 * We usually take a Genome, throw it through ChromRegionIterator, (sometimes) 
 * map those NamedRegions down to Regions, and then concatenate that Iterator
 * with the Expander of interest.  This just does all that, in its execute method --
 * it is an Expander which takes a Genome, and returns "all of the X's" in that 
 * entire genome.  
 * 
 * @author tdanford
 */
public class GenomeExpander<X> implements Expander<Genome,X> {
	
	private Expander<Region,X> expander;
	private Mapper<NamedRegion,Region> caster;
	
	public GenomeExpander(Expander<Region,X> exp) { 
		expander = exp;
		caster = new CastingMapper<NamedRegion,Region>();
	}

	public Iterator<X> execute(Genome a) {
        ChromRegionIterator chroms = new ChromRegionIterator(a);
        Iterator<Region> rchroms = new MapperIterator<NamedRegion,Region>(caster, chroms);
		return new ExpanderIterator<Region,X>(expander, rchroms);
	}

}
