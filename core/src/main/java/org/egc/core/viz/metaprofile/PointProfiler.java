/*
 * Author: tdanford
 * Date: Aug 12, 2008
 */
package org.egc.core.viz.metaprofile;

import org.egc.core.genome.location.Point;
import org.egc.core.gsebricks.verbs.Filter;

public interface PointProfiler<PointClass extends Point, ProfileClass extends Profile> extends Filter<PointClass,ProfileClass> { 
	public BinningParameters getBinningParameters();
	public void cleanup();
}
