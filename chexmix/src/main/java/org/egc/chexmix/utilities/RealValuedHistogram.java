package org.egc.chexmix.utilities;

import java.util.*;

import org.egc.core.utils.Pair;

public class RealValuedHistogram {

    private double start, stop;
    private double[] bins;
    private double binWidth;
    private double totalCount;

    public RealValuedHistogram(double start, double stop, int divs) {
        bins = new double[divs+2];
        for(int i = 0; i < bins.length; i++) { bins[i] = 0; }
        totalCount = 0;
        this.start = start;
        this.stop = stop;
        binWidth = (stop-start) / (double)divs;
    }

    public int getNumBins() { return bins.length; }
    public double getTotalCount() { return totalCount; }

    public double getMaxBinCount() {
        double max = 0;
        for(int x = 0; x < bins.length; x++) { if(bins[x] > max) { max = bins[x]; } }
        return max;
    }

    public double getMinBinCount() {
        double min = Integer.MAX_VALUE;
        for(int x = 0; x < bins.length; x++) { if(bins[x] < min) { min = bins[x]; } }
        return min;
    }

    public void addToBin(int b, double c) { bins[b] += c; totalCount += c; }

    public double getBin(int b) { return bins[b]; }
    public double[] getBins() { return bins; }

    public double getBinFraction(int b) {
        if(totalCount == 0) { return 0.0; }
        return bins[b] / totalCount;
    }

    public double[] cumulativeFromLeft() {
        double[] cdf = new double[bins.length];
        double seen = 0;
        for(int i = 0; i < cdf.length; i++) {
            seen += bins[i];
            cdf[i] = (totalCount > 0) ? seen / totalCount : 0.0;
        }
        return cdf;
    }

    public double[] cumulativeFromRight() {
        double[] cdf = new double[bins.length];
        double seen = 0;
        for(int i = cdf.length-1; i >= 0; i--) {
            seen += bins[i];
            cdf[cdf.length-1-i] = (totalCount > 0) ? seen / totalCount : 0.0;
        }
        return cdf;
    }

    public void addValue(int v) { addValue((double)v, 1.0); }
    public void addValue(double v) { addValue(v, 1.0); }
    public void addValue(double v, double weight) {
        if(v < start) { addToBin(0, weight); return; }
        if(v >= stop)  { addToBin(getNumBins()-1, weight); return; }
        int bin = (int)Math.floor((v-start) / binWidth) + 1;
        addToBin(bin, weight);
    }

    public void addValues(ArrayList<Double> vals) {
        for(Double d : vals) { addValue(d); }
    }

    public void addValueRange(double v1, double v2) { addValueRange(v1, v2, 1.0); }
    public void addValueRange(double v1, double v2, double weight) {
        double vstart = Math.max(v1, start);
        double vend   = Math.min(v2, stop);
        double v = vstart;
        while(v <= vend) {
            int bin = (int)Math.floor((v-start) / binWidth) + 1;
            addToBin(bin, weight);
            v += binWidth;
        }
    }

    public int getBinContainingVal(double val) {
        if(val < start) { return 0; }
        if(val >= stop)  { return getNumBins()-1; }
        return (int)Math.floor((val-start) / binWidth) + 1;
    }

    public Pair<Double,Double> getBinBounds(int b) {
        Double left = null, right = null;
        if(b == 0) {
            right = start;
        } else if(b == getNumBins()-1) {
            left = stop;
        } else {
            int effB = b - 1;
            left  = start + (double)effB       * binWidth;
            right = start + (double)(effB + 1) * binWidth;
        }
        return new Pair<>(left, right);
    }

    public void addHistogram(RealValuedHistogram h) {
        if(getNumBins() != h.getNumBins()) {
            System.err.println("Bin numbers not the same; cannot add histograms");
        } else {
            for(int x = 0; x < bins.length; x++) { bins[x] += h.getBin(x); }
        }
    }

    public void subtractHistogram(RealValuedHistogram h) {
        if(getNumBins() != h.getNumBins()) {
            System.err.println("Bin numbers not the same; cannot subtract histograms");
        } else {
            for(int x = 0; x < bins.length; x++) { bins[x] -= h.getBin(x); }
        }
    }

    public void divideByHistogram(RealValuedHistogram h) {
        if(getNumBins() != h.getNumBins()) {
            System.err.println("Bin numbers not the same; cannot divide histograms");
        } else {
            for(int x = 0; x < bins.length; x++) {
                if(h.getBin(x) > 0) { bins[x] = bins[x] / h.getBin(x); }
            }
        }
    }

    public double getHistoStart() { return start; }
    public double getHistoStop()  { return stop; }

    public void printContents() { printContents(false); }
    public void printContents(boolean toErr) {
        java.io.PrintStream out = toErr ? System.err : System.out;
        out.println(String.format("Less\t%f", bins[0]));
        for(int i = 1; i < bins.length-1; i++) {
            out.println(String.format("%f\t%f", start + ((double)(i-1) * binWidth), bins[i]));
        }
        out.println(String.format("More\t%f", bins[bins.length-1]));
    }

    public String contentsToString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Less\t%.2f\n", bins[0]));
        for(int i = 1; i < bins.length-1; i++) {
            sb.append(String.format("%.1f\t%.2f\n", start + ((double)(i-1) * binWidth), bins[i]));
        }
        sb.append(String.format("More\t%.2f\n", bins[bins.length-1]));
        return sb.toString();
    }
}
