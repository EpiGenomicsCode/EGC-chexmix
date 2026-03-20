/*
 * Author: tdanford
 * Date: Aug 28, 2008
 */
package org.egc.core.ml.regression;

import org.egc.core.gseutils.models.Model;


public class XYPoint extends Model {
	public Double x, y;
	
	public XYPoint() {}
	public XYPoint(Double _x, Double _y) { x = _x; y = _y; }
}
