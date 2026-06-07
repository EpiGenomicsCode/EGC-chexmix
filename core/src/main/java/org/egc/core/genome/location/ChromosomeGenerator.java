package org.egc.core.genome.location;

import java.util.*;
import java.util.function.Function;

import org.egc.core.genome.Genome;


/* gives an iterator over all of the chromosomes in a genome */

public class ChromosomeGenerator<X extends Genome> implements Function<X, Iterator<Region>> {

    public Iterator<Region> apply(X genome) {
        List<String> names = genome.getChromList();
        List<Region> chroms = new ArrayList<Region>();
        for (int i = 0; i < names.size(); i++) {
            chroms.add(new Region(genome,
                                  names.get(i),
                                  1,
                                  genome.getChromLength(names.get(i))));
        }
        return chroms.iterator();
    }
}
