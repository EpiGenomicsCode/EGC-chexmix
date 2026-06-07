package org.egc.core.genome.location;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.function.Function;

import org.egc.core.data.io.parsing.TranscriptFileExpander;
import org.egc.core.genome.Genome;

public class AnnotationLoader {

	protected Function<Region, Iterator<? extends Region>> annotExpander;
	protected String sourceName;
	protected int maxAnnotDist = 50000;
	protected boolean overlapOnly = false;

	public AnnotationLoader(Genome gen, String name, String type, int maxAnnotDist, boolean overlapOnly) {
		sourceName = name;
		this.maxAnnotDist = maxAnnotDist;
		this.overlapOnly = overlapOnly;

		if (type.equals("file")) {
			TranscriptFileExpander<Region> exp = new TranscriptFileExpander<>(gen, name);
			annotExpander = exp::apply;
		}
	}

	public Collection<Region> getAnnotations(Region coords) {
		ArrayList<Region> results = new ArrayList<Region>();
		Region query = overlapOnly ? coords : coords.expand(maxAnnotDist, maxAnnotDist);
		Iterator<? extends Region> iter = annotExpander.apply(query);
		while (iter.hasNext()) {
			results.add(iter.next());
		}
		return results;
	}

	/** Return genes if the expander is over genes. */
	public Collection<Gene> getGenes(Region coords) {
		ArrayList<Gene> results = new ArrayList<Gene>();
		Region query = overlapOnly ? coords : coords.expand(maxAnnotDist, maxAnnotDist);
		Iterator<? extends Region> iter = annotExpander.apply(query);
		while (iter.hasNext()) {
			Region r = iter.next();
			if (r instanceof Gene) { results.add((Gene) r); }
		}
		return results;
	}

	/** Return genes if the expander is over genes. */
	public Collection<Gene> getGenes(Point point) {
		ArrayList<Gene> results = new ArrayList<Gene>();
		Region query = overlapOnly ? point.expand(2) : point.expand(maxAnnotDist);
		Iterator<? extends Region> iter = annotExpander.apply(query);
		while (iter.hasNext()) {
			Region r = iter.next();
			if (r instanceof Gene) { results.add((Gene) r); }
		}
		return results;
	}
}
