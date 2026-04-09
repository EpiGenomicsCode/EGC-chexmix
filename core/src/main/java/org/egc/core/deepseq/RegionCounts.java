package org.egc.core.deepseq;

import java.util.ArrayList;
import java.util.List;

/**
 * RegionCounts holds pre-extracted read data for a genomic region, 
 * indexed by replicate. Stores positions, strands, and counts as
 * primitive arrays to avoid per-hit object allocation.
 * 
 * Replaces List&lt;List&lt;StrandedBaseCount&gt;&gt; in the hot path
 * between HitCache and the EM/ML algorithms.
 */
public class RegionCounts {

	private final int numReplicates;
	private final int[][] positions;     // [repIndex][hitIndex]
	private final boolean[][] plusStrand; // [repIndex][hitIndex]
	private final float[][] counts;      // [repIndex][hitIndex]
	private final int[] hitNum;          // [repIndex] -> count of hits

	public RegionCounts(int numReplicates) {
		this.numReplicates = numReplicates;
		positions = new int[numReplicates][];
		plusStrand = new boolean[numReplicates][];
		counts = new float[numReplicates][];
		hitNum = new int[numReplicates];
	}

	/**
	 * Set data for a replicate from two BaseHits slices (plus and minus strand).
	 */
	public void setFromHits(int repIndex, 
			int[] plusPos, float[] plusCounts, int plusStart, int plusEnd,
			int[] minusPos, float[] minusCounts, int minusStart, int minusEnd) {
		int plusSize = plusEnd - plusStart;
		int minusSize = minusEnd - minusStart;
		int total = plusSize + minusSize;
		hitNum[repIndex] = total;
		if (total == 0) {
			positions[repIndex] = new int[0];
			plusStrand[repIndex] = new boolean[0];
			counts[repIndex] = new float[0];
			return;
		}
		int[] pos = new int[total];
		boolean[] strand = new boolean[total];
		float[] cnt = new float[total];
		int idx = 0;
		for (int i = plusStart; i < plusEnd; i++) {
			pos[idx] = plusPos[i];
			strand[idx] = true;
			cnt[idx] = plusCounts[i];
			idx++;
		}
		for (int i = minusStart; i < minusEnd; i++) {
			pos[idx] = minusPos[i];
			strand[idx] = false;
			cnt[idx] = minusCounts[i];
			idx++;
		}
		positions[repIndex] = pos;
		plusStrand[repIndex] = strand;
		counts[repIndex] = cnt;
	}

	public int getNumReplicates() { return numReplicates; }
	public int getHitCount(int rep) { return hitNum[rep]; }
	public int[] getPositions(int rep) { return positions[rep]; }
	public boolean[] getPlusStrand(int rep) { return plusStrand[rep]; }
	public float[] getCounts(int rep) { return counts[rep]; }

	/**
	 * Total count weight for a replicate.
	 */
	public float getCountTotal(int rep) {
		float total = 0;
		for (int i = 0; i < hitNum[rep]; i++)
			total += counts[rep][i];
		return total;
	}

	/**
	 * Total hit count across all replicates.
	 */
	public int getTotalHitCount() {
		int total = 0;
		for (int i = 0; i < numReplicates; i++)
			total += hitNum[i];
		return total;
	}

	/**
	 * Convert to legacy List&lt;List&lt;StrandedBaseCount&gt;&gt; format for consumers
	 * not yet updated to use RegionCounts directly. Allocates StrandedBaseCount objects.
	 * @deprecated Use primitive array accessors instead for new code.
	 */
	public List<List<StrandedBaseCount>> toLegacyFormat() {
		List<List<StrandedBaseCount>> data = new ArrayList<>();
		for (int rep = 0; rep < numReplicates; rep++) {
			List<StrandedBaseCount> bases = new ArrayList<>(hitNum[rep]);
			for (int i = 0; i < hitNum[rep]; i++) {
				bases.add(new StrandedBaseCount(plusStrand[rep][i] ? '+' : '-', positions[rep][i], counts[rep][i]));
			}
			data.add(bases);
		}
		return data;
	}
}
