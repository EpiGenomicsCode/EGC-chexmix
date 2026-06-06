package org.egc.core.deepseq.experiments;

class PairedCounts implements Comparable<PairedCounts> {
	public Double x, y;

	public PairedCounts(double a, double b) {
		x = a;
		y = b;
	}

	/** Sort on increasing X */
	public int compareTo(PairedCounts pc) {
		if (x < pc.x) { return -1; }
		if (x > pc.x) { return 1; }
		return 0;
	}

	/** Compare based on the sum of both paired counts */
	public int compareByTotal(PairedCounts pc) {
		if ((x + y) < (pc.x + pc.y)) { return -1; }
		if ((x + y) > (pc.x + pc.y)) { return 1; }
		return 0;
	}
}
