package org.egc.core.ml.clustering.affinitypropagation;

import java.util.HashMap;
import java.util.Vector;

import org.egc.core.ml.clustering.Clusterable;
import org.egc.core.ml.clustering.ClusterablePair;


/**
 *
 * @author reeder
 *
 */
public class APCluster {

	public static double cluster(Vector<Clusterable> objects, SimilarityMeasure<Clusterable> s, double lam, int convit, int maxit) {

		s.addNoise();
		HashMap<ClusterablePair, Double> a = new HashMap<ClusterablePair, Double>();
		HashMap<ClusterablePair, Double> r = new HashMap<ClusterablePair, Double>();
		double max1 = SimilarityMeasure.NEGINF, max2 = SimilarityMeasure.NEGINF;
		int i1=0;
		double tmp = 0.0;
		int it = 0, decit = 0;
		boolean done = false;
		int[][] e = new int[s.size()][convit];
		int[] se = new int[s.size()];
		int decsumc = 0, decsum0 = 0;

		//initialize variables
		for (int i=0; i<s.size(); i++) {
			for (int j=0; j<s.size(); j++) {
				ClusterablePair ijpair = new ClusterablePair(objects.get(i), objects.get(j));
				if (s.exists(ijpair)) {
					a.put(ijpair, 0.0);
					r.put(ijpair, 0.0);
				}
			}
			for (int j=0; j<convit; j++) {
				e[i][j] = 0;
			}
			se[i] = 0;
		}

		while (!done) {
			//compute responsibilities
			for (int i=0; i<s.size(); i++) {
				max1 = SimilarityMeasure.NEGINF; max2 = SimilarityMeasure.NEGINF;
				i1=0;
				for (int k=0; k<s.size(); k++) {
					ClusterablePair ikpair = new ClusterablePair(objects.get(i), objects.get(k));
					if (!s.exists(ikpair)) {
						continue;
					}
					tmp = a.get(ikpair) + s.evaluate(ikpair);
					if (tmp > max1) {
						max2 = max1;
						max1 = tmp;
						i1 = k;
					} else if (tmp > max2) {
						max2 = tmp;
					}
				}
				for (int k=0; k<s.size(); k++) {
					ClusterablePair ikpair = new ClusterablePair(objects.get(i), objects.get(k));
					if (!s.exists(ikpair)) continue;
					if (k==i1) {
						double message = lam*r.get(ikpair) + (1.0-lam)*(s.evaluate(ikpair) - max2);
						r.put(ikpair, message);
					} else {
						double message = lam*r.get(ikpair) + (1.0-lam)*(s.evaluate(ikpair) - max1);
						r.put(ikpair, message);
					}
				}
			}

			//compute availabilities
			for (int k=0; k<s.size(); k++) {
				tmp = 0.0;
				for (int i=0; i<s.size(); i++) {
					ClusterablePair ikpair = new ClusterablePair(objects.get(i), objects.get(k));
					if (!s.exists(ikpair)) continue;
					if ((i!=k)&&(r.get(ikpair)>0)) {
						tmp += r.get(ikpair);
					}
				}
				for (int i=0; i<s.size(); i++) {
					ClusterablePair ikpair = new ClusterablePair(objects.get(i), objects.get(k));
					if (!s.exists(ikpair)) continue;
					double tmp2 = tmp;
					double ikr = r.get(ikpair);
					if ((i!=k)&&(ikr>0)) {
						tmp2 -= ikr;
					}
					if (i!=k) {
						tmp2 += r.get(new ClusterablePair(objects.get(k),objects.get(k)));
					}
					if (i==k) {
						double message = lam*a.get(ikpair) + (1.0 - lam)*tmp2;
						a.put(ikpair, message);
					} else if (tmp2 < 0) {
						double message = lam*a.get(ikpair) + (1.0-lam)*tmp2;
						a.put(ikpair, message);
					} else {
						double message = lam*a.get(ikpair);
						a.put(ikpair, message);
					}
				}
			}

			//check for convergence
			for (int j = 0; j<s.size(); j++) {
				ClusterablePair jjpair = new ClusterablePair(objects.get(j), objects.get(j));
				if (a.get(jjpair)+r.get(jjpair) > 0) {
					e[j][decit] = 1;
				} else {
					e[j][decit] = 0;
				}
				se[j] = 0;
				for (int d=0; d<convit; d++) {
					se[j] += e[j][d];
				}
			}

			decsumc = 0;
			decsum0 = 0;
			for (int j = 0; j<s.size(); j++) {
				if (se[j]==convit) {
					decsumc++;
				} else if (se[j]==0) {
					decsum0++;
				}
			}
			if (((decsumc+decsum0==s.size())&&(decsumc>0))||(it>=maxit)) {
				done = true;
				decit--;
			}

			it++; decit++;
			if (decit>=convit) {
				decit = 0;
			}
		}

		System.out.println("iterations: "+it);
		//compute assignments to exemplars
		//e[][decit] represents the exemplars
		//decsumc is the number of exemplars
		Vector<Integer> exidx = new Vector<Integer>();
		int[] assgn = new int[s.size()];

		for (int i=0; i<s.size(); i++) {
			if (e[i][decit]==1) {
				exidx.add(i);
			}
		}
		for (int i=0; i<s.size(); i++) {
			max1 = SimilarityMeasure.NEGINF;
			for (int j=0; j<exidx.size(); j++) {
				ClusterablePair ijpair = new ClusterablePair(objects.get(i), objects.get(exidx.get(j)));
				if (s.evaluate(ijpair) > max1) {
					max1 = s.evaluate(ijpair);
					assgn[i] = j;
				}
			}
		}
		for (int i=0; i<decsumc; i++) {
			assgn[exidx.get(i)] = i;
		}

		s.putAssignments(assgn);
		s.putExemplars(toIntArray(exidx));

		double netsim = 0.0;
		for (int i=0; i<s.size(); i++) {
    		netsim += s.evaluate(new ClusterablePair(objects.get(i), objects.get(exidx.get(assgn[i]))));
    	}

		return netsim;
	}

	public static int[] toIntArray(Vector<Integer> vec) {
		int[] toreturn = new int[vec.size()];
		for (int i=0; i<toreturn.length; i++) {
			toreturn[i] = vec.get(i);
		}
		return toreturn;
	}

}
