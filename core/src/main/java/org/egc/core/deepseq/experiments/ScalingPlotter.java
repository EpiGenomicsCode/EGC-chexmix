package org.egc.core.deepseq.experiments;

import java.awt.Color;
import java.io.File;
import java.io.IOException;

import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;

import org.egc.core.viz.scatter.ScatterPlot;

import Jama.Matrix;

class ScalingPlotter extends ScatterPlot {

	public ScalingPlotter(String title) {
		super(title);
	}

	void saveXYplot(Matrix datapoints, double scalingTotal, double scalingRatio,
			String xName, String yName, String outFilename, boolean rasterImage) {
		this.setWidth(800);
		this.setHeight(800);
		this.addDataset("other", datapoints, new Color(75, 75, 75, 80), 3);
		this.setXAxisLabel(xName);
		this.setYAxisLabel(yName);
		this.setXLogScale(true);
		this.setYLogScale(true);
		this.setXRangeFromData();
		this.setYRangeFromData();
		if (raxis.getRange().getLowerBound() > 0.1) {
			raxis.setLowerBound(0.1);
		}
		this.addDomainMarker(scalingTotal);
		this.addRangeMarker(scalingRatio);

		if (daxis instanceof NumberAxis)
			((NumberAxis) daxis).setTickUnit(new NumberTickUnit(5));
		if (raxis instanceof NumberAxis)
			((NumberAxis) raxis).setTickUnit(new NumberTickUnit(5));

		try {
			this.saveImage(new File(outFilename), width, height, rasterImage);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
