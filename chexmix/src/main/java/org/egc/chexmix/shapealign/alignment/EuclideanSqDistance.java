package org.egc.chexmix.shapealign.alignment;

/** Squared Euclidean Distance between two stranded tag distributions. */
public class EuclideanSqDistance extends ShapeMetric {

	public EuclideanSqDistance(double[][] arrayA, double[][] arrayB) {
		super(arrayA, arrayB);
	}

	public double computeDistance() {
		double[][] cat = buildCatArrays();
		double[] catA = cat[0], catB = cat[1], catRev = cat[2];

		double distF = 0.0, distR = 0.0;
		for (int i = 0; i < catA.length; i++) {
			distF += (catA[i] - catB[i])   * (catA[i] - catB[i]);
			distR += (catA[i] - catRev[i]) * (catA[i] - catRev[i]);
		}

		if (distF < distR) { revAlign = false; return distF; }
		else               { revAlign = true;  return distR; }
	}
}
