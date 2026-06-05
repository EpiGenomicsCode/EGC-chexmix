package org.egc.chexmix.utilities;

import org.egc.core.genome.location.Point;

public interface PointProfiler<PointClass extends Point, ProfileClass extends Profile> {
	public ProfileClass execute(PointClass a);
	public BinningParameters getBinningParameters();
	public void cleanup();
}
