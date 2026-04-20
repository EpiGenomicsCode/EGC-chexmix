package org.egc.chexmix.shapealign.alignment;

import org.egc.core.math.stats.StatUtil;

/** KL-divergence between two stranded tag distributions. */
public class KLDivergence extends ShapeMetric {

	public KLDivergence(double[][] arrayA, double[][] arrayB) {
		super(arrayA, arrayB);
	}

	public double computeDistance() {
		double[][] cat = buildCatArrays();
		double[] catA = cat[0], catB = cat[1], catRev = cat[2];

		StatUtil.mutate_normalize(catA);
		StatUtil.mutate_normalize(catB);
		StatUtil.mutate_normalize(catRev);

		double distF = 0.0, distR = 0.0;
		for (int i = 0; i < catA.length; i++) {
			distF += catA[i] * (Math.log(catA[i]) - Math.log(catB[i]));
			distR += catA[i] * (Math.log(catA[i]) - Math.log(catRev[i]));
		}

		if (distF < distR) { revAlign = false; return distF; }
		else               { revAlign = true;  return distR; }
	}
}
