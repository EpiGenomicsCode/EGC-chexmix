package org.egc.chexmix.shapealign.alignment;

/**
 * Common base for strand-aware shape similarity metrics.
 * Holds the shared fields, constructor, and strand concatenation logic.
 */
public abstract class ShapeMetric {

	protected double[][] normAarray;
	protected double[][] normBarray;
	protected double[][] revNormBarray;
	protected int window;
	protected boolean revAlign = false;

	public ShapeMetric(double[][] arrayA, double[][] arrayB) {
		normAarray = arrayA;
		normBarray = arrayB;
		window = arrayB.length - 1;
		revNormBarray = new double[window + 1][2];
		for (int i = 0; i <= window; i++) {
			for (int s = 0; s < 2; s++) {
				revNormBarray[window - i][1 - s] = arrayB[i][s];
			}
		}
	}

	public boolean isReverse() { return revAlign; }

	/**
	 * Concatenates forward and reverse strands from normAarray, normBarray, and
	 * revNormBarray into flat 1-D arrays. Returns { catA, catB, catRev }.
	 */
	protected double[][] buildCatArrays() {
		int len = 2 * (window + 1);
		double[] catA = new double[len];
		double[] catB = new double[len];
		double[] catRev = new double[len];
		for (int i = 0; i <= window; i++) {
			catA[i]   = normAarray[i][0];    catA[i + window + 1]   = normAarray[i][1];
			catB[i]   = normBarray[i][0];    catB[i + window + 1]   = normBarray[i][1];
			catRev[i] = revNormBarray[i][0]; catRev[i + window + 1] = revNormBarray[i][1];
		}
		return new double[][] { catA, catB, catRev };
	}
}
