package org.egc.core.math.stats;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.egc.core.utils.Pair;


public class StatUtil {

	/**
	 * Sorts the array and returns the positions of the original array corresponding
	 * to the ordered elements. Accepts only primitive integers.
	 */
	public static int[] findSort(int[] a) {
		int[] sortedInds = new int[a.length];
		Map<Integer, ArrayList<Integer>> val2Index = new HashMap<Integer, ArrayList<Integer>>();

		for(int i = 0; i < a.length; i++) {
			if(!val2Index.containsKey(a[i]))
				val2Index.put(a[i], new ArrayList<Integer>());
			val2Index.get(a[i]).add(i);
		}

		Arrays.sort(a);
		Set<Integer> uniqueEls = new LinkedHashSet<Integer>();
		for(int i = 0; i < a.length; i++) { uniqueEls.add(a[i]); }
		int count = 0;
		for(Object key : uniqueEls.toArray()) {
			for(Integer idx : val2Index.get(key))
				sortedInds[count++] = idx;
		}
		return sortedInds;
	}

	/**
	 * Sorts the double array and returns the positions of the original array
	 * corresponding to the ordered elements.
	 */
	public static int[] findSort(double[] a) {
		int[] sortedInds = new int[a.length];
		Map<Double, ArrayList<Integer>> val2Index = new HashMap<Double, ArrayList<Integer>>();

		for(int i = 0; i < a.length; i++) {
			if(!val2Index.containsKey(a[i]))
				val2Index.put(a[i], new ArrayList<Integer>());
			val2Index.get(a[i]).add(i);
		}
		Arrays.sort(a);
		Set<Double> uniqueEls = new LinkedHashSet<Double>();
		for(int i = 0; i < a.length; i++) { uniqueEls.add(a[i]); }
		int count = 0;
		for(Object key : uniqueEls.toArray()) {
			for(Integer idx : val2Index.get(key))
				sortedInds[count++] = idx;
		}
		return sortedInds;
	}

	/** @see StatUtil#findSort(Object[], Comparator) */
	public static <T> int[] findSort(T[] a) {
		return findSort(a, null);
	}

	/**
	 * Orders the elements of the array in ascending order (based on the Comparator)
	 * and returns the indexes of the sorted elements in the original array.
	 */
	public static <T> int[] findSort(T[] a, Comparator<? super T> c) {
		int[] sortedInds = new int[a.length];
		Map<T, ArrayList<Integer>> val2Index = new HashMap<T, ArrayList<Integer>>();
		for(int i = 0; i < a.length; i++) {
			if(!val2Index.containsKey(a[i]))
				val2Index.put(a[i], new ArrayList<Integer>());
			val2Index.get(a[i]).add(i);
		}
		Arrays.sort(a, c);
		Set<T> uniqueEls = new LinkedHashSet<T>(Arrays.asList(a));
		int count = 0;
		for(Object key : uniqueEls.toArray()) {
			for(Integer idx : val2Index.get(key))
				sortedInds[count++] = idx;
		}
		return sortedInds;
	}

	/**
	 * Returns the maximum of this array and all positions where it occurs.
	 */
	public static <T extends Comparable<T>> Pair<T, TreeSet<Integer>> findMax(T[] x) {
		if(x.length == 0)
			throw new IndexOutOfBoundsException("Hey dude, you cannot enter an empty array...");
		T maximum = x[0];
		TreeSet<Integer> max_index = new TreeSet<Integer>();
		max_index.add(0);
		int count = 0;
		for(T el : x) {
			if(el.compareTo(maximum) > 0) {
				maximum = el;
				max_index.clear();
				max_index.add(count);
			} else if(el.compareTo(maximum) == 0) {
				max_index.add(count);
			}
			count++;
		}
		return new Pair<T, TreeSet<Integer>>(maximum, max_index);
	}

	/**
	 * Returns the maximum of this double array and all positions where it occurs.
	 */
	public static Pair<Double, TreeSet<Integer>> findMax(double[] x) {
		if(x.length == 0)
			throw new IndexOutOfBoundsException("Hey dude, you cannot enter an empty array...");
		Double maximum = x[0];
		TreeSet<Integer> max_index = new TreeSet<Integer>();
		max_index.add(0);
		int count = 0;
		for(Double el : x) {
			if(el.compareTo(maximum) > 0) {
				maximum = el;
				max_index.clear();
				max_index.add(count);
			} else if(el.compareTo(maximum) == 0) {
				max_index.add(count);
			}
			count++;
		}
		return new Pair<Double, TreeSet<Integer>>(maximum, max_index);
	}

	/** Permutes a primitive int array by the given index permutation. */
	public static int[] permute(int[] a, int[] permInds) {
		if(permInds.length != a.length)
			throw new IllegalArgumentException("a and permInds must have the same length.");
		int[] temp = new int[a.length];
		for(int i = 0; i < a.length; i++)
			temp[i] = a[permInds[i]];
		return temp;
	}

	/** Permutes a float array by the given index permutation. */
	public static float[] permute(float[] a, int[] permInds) {
		if(permInds.length != a.length)
			throw new IllegalArgumentException("a and permInds must have the same length.");
		float[] temp = new float[a.length];
		for(int i = 0; i < a.length; i++)
			temp[i] = a[permInds[i]];
		return temp;
	}

	/** Permutes a char array by the given index permutation. */
	public static char[] permute(char[] a, int[] permInds) {
		if(permInds.length != a.length)
			throw new IllegalArgumentException("a and permInds must have the same length.");
		char[] temp = new char[a.length];
		for(int i = 0; i < a.length; i++)
			temp[i] = a[permInds[i]];
		return temp;
	}

	/** Permutes a generic array by the given index permutation. */
	@SuppressWarnings("unchecked")
	public static <T> T[] permute(T[] a, int[] permInds) {
		if(permInds.length != a.length)
			throw new IllegalArgumentException("a and permInds must have the same length.");
		int[] tempInds = permInds.clone();
		Arrays.sort(tempInds);
		for(int i = 0; i < tempInds.length; i++) {
			if(tempInds[i] != i)
				throw new IllegalArgumentException("permInds must have all numbers from 0 to permInds.length-1.");
		}
		T[] temp = (T[]) Array.newInstance(a.getClass().getComponentType(), a.length);
		for(int i = 0; i < a.length; i++)
			temp[i] = a[permInds[i]];
		return temp;
	}

	/** Smooth y[] using a cubic spline; stepSize and averageWidth control sampling density. */
	public static double[] cubicSpline(double[] y, int stepSize, int averageWidth) {
		if(stepSize < averageWidth)
			averageWidth = stepSize;
		double[] x = new double[y.length / stepSize];
		double[] yx = new double[y.length / stepSize];
		x[0] = 0;
		yx[0] = 0;
		for(int j = 0; j < averageWidth; j++)
			yx[0] += y[j] / averageWidth;
		for(int i = 1; i < x.length - 1; i++) {
			x[i] = i * stepSize + stepSize / 2;
			yx[i] = 0;
			for(int j = 0; j < averageWidth; j++)
				yx[i] += y[(int) x[i] + (j - averageWidth / 2)] / averageWidth;
		}
		x[x.length - 1] = y.length - 1;
		yx[x.length - 1] = 0;
		for(int j = 0; j < averageWidth; j++)
			yx[x.length - 1] += y[y.length - 1 - j] / averageWidth;
		CubicSpline cs = new CubicSpline(x, yx);
		double[] yy = new double[y.length];
		for(int i = 0; i < y.length; i++)
			yy[i] = cs.interpolate(i);
		return yy;
	}

	/** Smooth Y[] using a Gaussian kernel of width (stdev). Result is renormalized. */
	public static double[] gaussianSmoother(double[] Y, double width) {
		int length = Y.length;
		double[] yy = new double[length];
		double[] yy_weight = new double[length];
		double variance = width * width;
		double gaussCoeff = 1.0 / Math.sqrt(2.0 * Math.PI * variance);
		double total = 0;
		for(int i = 0; i < length; i++) {
			yy[i] = 2.0E-300;
			yy_weight[i] = 2.0E-300;
			for(int j = 0; j < length; j++) {
				double diff = (double) (j - i);
				double gaussianProb = gaussCoeff * Math.exp(-(diff * diff) / (2.0 * variance));
				yy[i] += Y[j] * gaussianProb;
				yy_weight[i] += gaussianProb;
			}
			yy[i] /= yy_weight[i];
			total += yy[i];
		}
		for(int i = 0; i < length; i++)
			yy[i] /= total;
		return yy;
	}

	/** Smooth Y[] using a symmetric kernel (e.g. Gaussian). Result is renormalized. */
	public static double[] symmetricKernelSmoother(double[] Y, double[] kernel) {
		int length = Y.length;
		int kernel_length = kernel.length;
		double[] yy = new double[length];
		double total = 0;
		for(int i = 0; i < length; i++) {
			double v = kernel[0] * Y[i] + 2.0E-300;
			double weight = kernel[0];
			for(int j = 1; j < kernel_length && i + j < length; j++) {
				v += Y[i + j] * kernel[j];
				weight += kernel[j];
			}
			for(int j = 1; j < kernel_length && i - j >= 0; j++) {
				v += Y[i - j] * kernel[j];
				weight += kernel[j];
			}
			yy[i] = v / weight;
			total += yy[i];
		}
		for(int i = 0; i < length; i++)
			yy[i] /= total;
		return yy;
	}

	/** KL divergence for discrete distributions P and Q (http://en.wikipedia.org/wiki/Kullback-Leibler_divergence). */
	private static double KL_Divergence(double[] P, double[] Q) {
		double d = 0;
		double[] p = P.clone();
		double[] q = Q.clone();
		mutate_normalize(p);
		mutate_normalize(q);
		for(int i = 0; i < p.length; i++)
			d += p[i] * (Math.log(p[i]) - Math.log(q[i]));
		return d;
	}

	/** log of KL divergence between P and Q. */
	public static double log_KL_Divergence(double[] P, double[] Q) {
		return Math.log(KL_Divergence(P, Q));
	}

	/** Normalize dist[] in-place; replaces values ≤ 0 with 1e-20 before normalizing. */
	public static void mutate_normalize(double[] dist) {
		double total = 0;
		for(int i = 0; i < dist.length; i++) {
			if(dist[i] <= 0)
				dist[i] = 1e-20;
			total += dist[i];
		}
		for(int i = 0; i < dist.length; i++)
			dist[i] /= total;
	}

}//end of StatUtil class
