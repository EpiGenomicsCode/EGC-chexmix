package org.egc.core.deepseq.experiments;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import Jama.Matrix;


/**
 * ExperimentScaler: calculate a scaling transformation between all Sample pairs in an ExperimentCondition
 * This is performed on the condition level so that a scaling can also be defined between pooled hits from all signals & controls
 *
 * @author Shaun Mahony
 * @version	%I%, %G%
 */
public class ExperimentScaler {

	public ExperimentScaler(){}

	/**
	 * Dispatch to whichever scaling method is selected by econfig.
	 * Safe to call for both per-replicate and pooled cases; totalSignal/totalControl
	 * are only consumed by the HitRatioAndNCIS path.
	 */
	public double computeScalingRatio(ExptConfig cfg, List<Float> signal, List<Float> control,
			double totalSignal, double totalControl, String plotFile) {
		if (cfg.getScalingBySES())
			return scalingRatioBySES(signal, control);
		else if (cfg.getScalingByRegression())
			return scalingRatioByRegression(signal, control);
		else if (cfg.getScalingByMedian())
			return scalingRatioByMedian(signal, control);
		else if (cfg.getScalingByHitRatioAndNCIS())
			return scalingRatioByHitRatioAndNCIS(signal, control, totalSignal, totalControl, plotFile, cfg.getNCISMinBinFrac());
		else
			return scalingRatioByNCIS(signal, control, plotFile, cfg.getNCISMinBinFrac());
	}

	/**
	 * Calculate a scaling ratio by fitting a line through the hit count pairs.
	 * Using a 10Kbp window, this is the same as PeakSeq with Pf=0.
	 *
	 * Fits the no-intercept OLS model x = beta * y, i.e. beta = sum(x*y) / sum(y^2).
	 * @return double
	 */
	public double scalingRatioByRegression(List<Float> setA, List<Float> setB){
		double scalingRatio=1;
		if(setA.size()!=setB.size()){
			throw new IllegalArgumentException("ExperimentScaler is trying to scale lists of two different lengths");
		}

		double sumXY = 0, sumYY = 0;
		for(int i=0; i<setA.size(); i++){
			double x = setA.get(i);
			double y = setB.get(i);
			sumXY += x * y;
			sumYY += y * y;
		}
		if (sumYY == 0) {
		    throw new IllegalArgumentException("setB contains only zeros; cannot compute scaling ratio");
		}
		else {
			scalingRatio = sumXY / sumYY;
		}
		return(scalingRatio);
	}

	/**
	 * Find the median hit count ratio in bins that have non-zero counts
	 * @return
	 */
	public double scalingRatioByMedian(List<Float> setA, List<Float> setB){
		double scalingRatio=1;
		if(setA.size()!=setB.size()){
			throw new IllegalArgumentException("ExperimentScaler is trying to scale lists of two different lengths");
		}

		ArrayList<Float> ratios = new ArrayList<Float>();
	    for(int x=0; x<setA.size(); x++){
			if(setA.get(x)>0 && setB.get(x)>0)
				ratios.add((float)(setA.get(x) / setB.get(x)));
        }
        Collections.sort(ratios);
		scalingRatio = ratios.get(ratios.size() / 2);
        return(scalingRatio);
	}

	/**
	 * Find the scaling ratio according to the SES method from Diaz, et al. Stat Appl Genet Mol Biol. 2012.
	 * Also sets a background proportion estimate for the signal channel.
	 * @return
	 */
	public double scalingRatioBySES(List<Float> setA, List<Float> setB){
		double scalingRatio=1;
		if(setA.size()!=setB.size()){
			throw new IllegalArgumentException("ExperimentScaler is trying to scale lists of two different lengths");
		}

		float totalA=0, totalB=0;
		List<PairedCounts> counts = new ArrayList<PairedCounts>();
		for(int x=0; x<setA.size(); x++){
			totalA += setA.get(x);
			totalB += setB.get(x);
			counts.add(new PairedCounts(setA.get(x), setB.get(x)));
		}

		Collections.sort(counts);

        //SES procedure
        double cumulA=0, cumulB=0, maxDiffAB=0, currDiff=0;
        for(PairedCounts pc : counts){
        	cumulA+=pc.x;
        	cumulB+=pc.y;
        	currDiff = (cumulB/totalB)-(cumulA/totalA);
        	if(currDiff>maxDiffAB && cumulA>0 && cumulB>0){
        		maxDiffAB=currDiff;
        		scalingRatio = cumulA/cumulB;
        	}
        }
		return(scalingRatio);
	}

	/**
	 * Find the scaling ratio according to the NCIS method from Liang & Keles (BMC Bioinf 2012).
	 * Also sets a background proportion estimate for the signal channel.
	 * Should be run using *all* genomic windows in the Lists.
	 * Uses ratios that are based on at least 75% of genomic regions by default.
	 * @param setA : signal list
	 * @param setB : control list
	 * @param outputFile : optional file that will contain the data
	 * @return
	 */
	public double scalingRatioByNCIS(List<Float> setA, List<Float> setB, String outputFile, double minFrac){
		double scalingRatio=1;
		double totalAtScaling=0;
		if(setA.size()!=setB.size()){
			throw new IllegalArgumentException("ExperimentScaler is trying to scale lists of two different lengths");
		}

		float numPairs = (float)setA.size();
		List<PairedCounts> counts = new ArrayList<PairedCounts>();
		for(int x=0; x<setA.size(); x++)
			counts.add(new PairedCounts(setA.get(x), setB.get(x)));

		//NCIS uses increasing total tag counts versus enrichment ratio
		Collections.sort(counts, new Comparator<PairedCounts>(){
            public int compare(PairedCounts o1, PairedCounts o2) {return o1.compareByTotal(o2);}
        });

        //NCIS procedure
        double cumulA=0, cumulB=0, currRatio=0, lastRatio=-1;
        float i=0;
        for(PairedCounts pc : counts){
        	cumulA+=pc.x;
        	cumulB+=pc.y;
        	totalAtScaling = pc.x+pc.y;

        	i++;
        	if(i/numPairs > minFrac && cumulA>0 && cumulB>0){ //NCIS estimates begin using the lower 3 quartiles of the genome (based on total tags)
	        	currRatio = (cumulA/cumulB);
	        	if(lastRatio==-1 || currRatio<lastRatio){
	        		lastRatio = currRatio;
	        	}else{
	        		break;
	        	}
        	}
        }
        scalingRatio = currRatio;

        if(outputFile != null)
        	generateScalingPlots(counts, totalAtScaling, scalingRatio, outputFile, "NCIS");

		return(scalingRatio);
	}

	/**
	 * Find the scaling ratio according to the total tag normalization followed by NCIS method from Liang & Keles (BMC Bioinf 2012).
	 * Also sets a background proportion estimate for the signal channel.
	 * Should be run using *all* genomic windows in the Lists.
	 * Uses ratios that are based on at least 75% of genomic regions by default.
	 * @param setA : signal list
	 * @param setB : control list
	 * @param outputFile : optional file that will contain the data
	 * @return
	 */
	public double scalingRatioByHitRatioAndNCIS(List<Float> setA, List<Float> setB, double totalA, double totalB, String outputFile, double minFrac){
		double scalingRatio=1;
		double totalAtScaling=0;
		if(setA.size()!=setB.size()){
			throw new IllegalArgumentException("ExperimentScaler is trying to scale lists of two different lengths");
		}

		//First normalize using total reads
		float tRatio = (float) (totalA/totalB);
		List<Float> setnB = new ArrayList<Float>();
		for (int x=0; x< setB.size();x++)
			setnB.add(setB.get(x)*tRatio);

		float numPairs = (float)setA.size();
		List<PairedCounts> counts = new ArrayList<PairedCounts>();
		for(int x=0; x<setA.size(); x++)
			counts.add(new PairedCounts(setA.get(x), setnB.get(x)));

		//NCIS uses increasing total tag counts versus enrichment ratio
		Collections.sort(counts, new Comparator<PairedCounts>(){
            public int compare(PairedCounts o1, PairedCounts o2) {return o1.compareByTotal(o2);}
        });

        //NCIS procedure
        double cumulA=0, cumulB=0, currRatio=0, lastRatio=-1;
        float i=0;
        for(PairedCounts pc : counts){
        	cumulA+=pc.x;
        	cumulB+=pc.y;
        	totalAtScaling = pc.x+pc.y;

        	i++;
        	if(i/numPairs > minFrac && cumulA>0 && cumulB>0){ //NCIS estimates begin using the lower 3 quartiles of the genome (based on total tags)
	        	currRatio = (cumulA/cumulB);
	        	if(lastRatio==-1 || currRatio<lastRatio){
	        		lastRatio = currRatio;
	        	}else{
	        		break;
	        	}
        	}
        }

        scalingRatio = currRatio*tRatio; //Multiply by the total tag normalization

        if(outputFile != null)
        	generateScalingPlots(counts, totalAtScaling, scalingRatio, outputFile, "TotalReadsAndNCIS");

		return(scalingRatio);
	}

	/**
	 * Calculate the background proportion of an IP experiment by correcting the scaling ratio by the read count ratio.
	 * Be careful with this method, there are a couple of assumptions:
	 *  - The scaling ratio was calculated between IP and control experiments
	 *  - The method used to calculate the scaling ratio attempted to normalize to background and not all regions (e.g. SES method attempts background normalization)
	 * @return
	 */
	public Double calculateBackgroundFromScalingRatio(ControlledExperiment expt){
		if(expt.getControlScaling()==-1)
			return(-1.0); //scaling not yet performed
		double ctrlCount = expt.getControl()==null ? expt.getSignal().getHitCount() : expt.getControl().getHitCount();
		return(expt.getControlScaling() / (expt.getSignal().getHitCount()/ctrlCount));
	}

	/**
	 * Shared plot + data-file generation used by both NCIS-based scaling methods.
	 * @param counts list sorted by ascending total (pc.x+pc.y)
	 * @param suffix filename segment distinguishing NCIS vs TotalReadsAndNCIS outputs
	 */
	private void generateScalingPlots(List<PairedCounts> counts, double totalAtScaling,
			double scalingRatio, String outputFile, String suffix) {
		// Cumulative ratio vs bin total
		List<Double> bintotals = new ArrayList<Double>();
		List<Double> ratios = new ArrayList<Double>();
		double cumulA = 0, cumulB = 0;
		for (PairedCounts pc : counts) {
			cumulA += pc.x;
			cumulB += pc.y;
			if (cumulA > 0 && cumulB > 0) {
				bintotals.add(pc.x + pc.y);
				ratios.add(cumulA / cumulB);
			}
		}
		Matrix dataToPlot = listPairsToMatrix(bintotals, ratios);

		// Marginal ratios vs bin totals
		List<Double> bintot = new ArrayList<Double>();
		List<Double> mratios = new ArrayList<Double>();
		for (int x = 0; x < counts.size(); x++) {
			PairedCounts pc = counts.get(x);
			if (pc.x > 0 && pc.y > 0) {
				double currA = pc.x, currB = pc.y;
				double currTot = pc.x + pc.y;
				while (x < counts.size() - 1 && (counts.get(x + 1).x + counts.get(x + 1).y) == currTot) {
					x++;
					pc = counts.get(x);
					currA += pc.x;
					currB += pc.y;
				}
				bintot.add(currTot);
				mratios.add(currA / currB);
			}
		}
		Matrix dataToPlot2 = listPairsToMatrix(bintot, mratios);

		// Generate images
		new ScalingPlotter(outputFile + " NCIS plot").saveXYplot(
				dataToPlot, totalAtScaling, scalingRatio,
				"Binned Total Tag Count", "Cumulative Count Scaling Ratio",
				outputFile + "." + suffix + "_scaling-ccr.png", true);
		new ScalingPlotter(outputFile + " NCIS plot").saveXYplot(
				dataToPlot2, totalAtScaling, scalingRatio,
				"Binned Total Tag Count", "Marginal Signal/Control Ratio",
				outputFile + "." + suffix + "_scaling-marginal.png", true);

		// Write data points to files
		try {
			try (FileWriter fout = new FileWriter(outputFile + "." + suffix + "_scaling-ccr.count")) {
				for (int d = 0; d < bintotals.size(); d++)
					fout.write(bintotals.get(d) + "\t" + ratios.get(d) + "\n");
			}
			try (FileWriter fout2 = new FileWriter(outputFile + "." + suffix + "_scaling-marginal.count")) {
				for (int d = 0; d < bintot.size(); d++)
					fout2.write(bintot.get(d) + "\t" + mratios.get(d) + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private Matrix listPairsToMatrix(List<Double> xs, List<Double> ys) {
		Matrix m = new Matrix(xs.size(), 2);
		for (int d = 0; d < xs.size(); d++) {
			m.set(d, 0, xs.get(d));
			m.set(d, 1, ys.get(d));
		}
		return m;
	}
}
