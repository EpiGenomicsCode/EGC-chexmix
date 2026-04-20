package org.egc.chexmix.shapealign.alignment;

/** Pearson and weighted-Pearson correlation between two stranded tag distributions. */
public class PearsonCorrelation extends ShapeMetric {

	public PearsonCorrelation(double[][] arrayA, double[][] arrayB) {
		super(arrayA, arrayB);
	}

	public double computePearsonCorr() {
		double[][] cat = buildCatArrays();
		double[] catA = cat[0], catB = cat[1], catRev = cat[2];

		double sumA = 0, sumB = 0;
		for (int i = 0; i < catA.length; i++) { sumA += catA[i]; sumB += catB[i]; }
		double aveA = sumA / catA.length;
		double aveB = sumB / catB.length;

		double covf = 0, covr = 0, varA = 0, varBf = 0, varBr = 0;
		for (int i = 0; i < catA.length; i++) {
			double ai   = catA[i]   - aveA;
			double bi_f = catB[i]   - aveB;
			double bi_r = catRev[i] - aveB;
			covf += ai * bi_f;  covr += ai * bi_r;
			varA += ai * ai;    varBf += bi_f * bi_f;  varBr += bi_r * bi_r;
		}
		double corrF = covf / (Math.sqrt(varA) * Math.sqrt(varBf));
		double corrR = covr / (Math.sqrt(varA) * Math.sqrt(varBr));

		if (corrF > corrR) { revAlign = false; return corrF; }
		else               { revAlign = true;  return corrR; }
	}

	public double computeWeightedPearsonCorr() {
		double[][] cat = buildCatArrays();
		double[] catA = cat[0], catB = cat[1], catRev = cat[2];

		double[] fweight = new double[catA.length];
		double[] rweight = new double[catA.length];
		for (int i = 0; i < catA.length; i++) {
			fweight[i] = (catA[i] + catB[i])   / 2;
			rweight[i] = (catA[i] + catRev[i]) / 2;
		}

		double wSumA = 0, wSumB = 0, wSumB_r = 0, wSum = 0;
		for (int i = 0; i < catA.length; i++) {
			wSumA   += catA[i]   * fweight[i];
			wSumB   += catB[i]   * fweight[i];
			wSumB_r += catRev[i] * rweight[i];
			wSum    += fweight[i];
		}
		double wAveA = wSumA / wSum, wAveB = wSumB / wSum, wAveB_r = wSumB_r / wSum;

		double covf = 0, covr = 0, varA = 0, varBf = 0, varBr = 0;
		for (int i = 0; i < catA.length; i++) {
			double ai   = catA[i]   - wAveA;
			double bi_f = catB[i]   - wAveB;
			double bi_r = catRev[i] - wAveB_r;
			covf  += fweight[i] * ai * bi_f;  covr  += rweight[i] * ai * bi_r;
			varA  += fweight[i] * ai * ai;
			varBf += fweight[i] * bi_f * bi_f;  varBr += rweight[i] * bi_r * bi_r;
		}
		double corrF = covf / (Math.sqrt(varA) * Math.sqrt(varBf));
		double corrR = covr / (Math.sqrt(varA) * Math.sqrt(varBr));

		if (corrF > corrR) { revAlign = false; return corrF; }
		else               { revAlign = true;  return corrR; }
	}
}
