package org.egc.chexmix.utilities;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;


/**
 * MetaProfileClustering: hierarchical clustering of profile rows for MetaProfileRenderer.
 * Consolidates ProfileClusteringHandler, ProfileClusterable, HierarchicalClustering,
 * ClusterNode, SingletonCluster, EuclideanDistance, and their associated interfaces.
 */
public class MetaProfileClustering {

    private final BinningParameters params;

    public MetaProfileClustering(BinningParameters bps) {
        params = bps;
    }

    /** Return a permutation of row indices sorted by hierarchical clustering. */
    public Vector<Integer> cluster(Vector<Profile> profs) {
        Vector<ProfileItem> items = new Vector<>();
        for (int i = 0; i < profs.size(); i++)
            items.add(new ProfileItem(i, profs.get(i)));

        Collection<ClusterNode> tree = hierarchicalCluster(items);

        Vector<Integer> indices = new Vector<>();
        for (ClusterNode node : tree)
            collectIndices(indices, node);
        return indices;
    }

    // ---- Hierarchical clustering (agglomerative, complete linkage) --------

    private Collection<ClusterNode> hierarchicalCluster(Vector<ProfileItem> items) {
        int n = items.size();
        Vector<ClusterNode> clusters = new Vector<>();
        Vector<ProfileItem> reps = new Vector<>();

        for (ProfileItem item : items) {
            clusters.add(new LeafNode(item));
            reps.add(item);
        }

        Double[][] distances = new Double[n][n];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                distances[i][j] = euclidean(reps.get(i), reps.get(j));

        int nclusters = clusters.size();
        while (nclusters > 1) {
            int mini = -1, minj = -1;
            double mindist = Double.MAX_VALUE;
            for (int i = 0; i < clusters.size() - 1; i++) {
                if (clusters.get(i) == null) continue;
                for (int j = i + 1; j < clusters.size(); j++) {
                    if (clusters.get(j) == null) continue;
                    double d = Double.isNaN(distances[i][j])
                            ? euclidean(reps.get(i), reps.get(j))
                            : distances[i][j];
                    if (!Double.isNaN(d) && d < mindist) {
                        mindist = d;
                        mini = i;
                        minj = j;
                    }
                }
            }
            if (mini == -1) break;

            InternalNode merged = new InternalNode(clusters.get(mini), clusters.get(minj));
            clusters.set(mini, merged);
            clusters.set(minj, null);
            reps.set(mini, representative(merged));
            reps.set(minj, null);
            for (int i = 0; i < n; i++) {
                distances[i][minj] = Double.NaN;
                distances[minj][i] = Double.NaN;
                distances[i][mini] = Double.NaN;
                distances[mini][i] = Double.NaN;
            }
            nclusters--;
        }

        Vector<ClusterNode> output = new Vector<>();
        for (ClusterNode c : clusters)
            if (c != null) output.add(c);
        return output;
    }

    private double euclidean(ProfileItem a, ProfileItem b) {
        if (a == null || b == null) return Double.NaN;
        int dim = a.dimension();
        if (dim != b.dimension()) throw new IllegalArgumentException("dimension mismatch");
        double sum = 0.0;
        for (int i = 0; i < dim; i++) {
            double diff = a.getValue(i) - b.getValue(i);
            sum += diff * diff;
        }
        return Math.sqrt(sum);
    }

    private ProfileItem representative(InternalNode node) {
        String name = "cluster-rep";
        MetaProfile mp = new MetaProfile(name, params);
        for (ProfileItem item : node.getItems())
            mp.addProfile(item.profile);
        return new ProfileItem(mp);
    }

    private void collectIndices(Vector<Integer> indices, ClusterNode node) {
        if (node instanceof InternalNode) {
            InternalNode inner = (InternalNode) node;
            collectIndices(indices, inner.left);
            collectIndices(indices, inner.right);
        } else {
            LeafNode leaf = (LeafNode) node;
            if (leaf.item.index != null)
                indices.add(leaf.item.index);
        }
    }

    // ---- Internal data structures -----------------------------------------

    private interface ClusterNode {
        Set<ProfileItem> getItems();
    }

    private static class LeafNode implements ClusterNode {
        final ProfileItem item;
        LeafNode(ProfileItem p) { item = p; }
        public Set<ProfileItem> getItems() {
            Set<ProfileItem> s = new HashSet<>();
            s.add(item);
            return s;
        }
    }

    private static class InternalNode implements ClusterNode {
        final ClusterNode left, right;
        InternalNode(ClusterNode l, ClusterNode r) { left = l; right = r; }
        public Set<ProfileItem> getItems() {
            Set<ProfileItem> s = new HashSet<>();
            s.addAll(left.getItems());
            s.addAll(right.getItems());
            return s;
        }
    }

    private static class ProfileItem {
        final Integer index;
        final Profile profile;

        ProfileItem(int idx, Profile p) { index = idx; profile = p; }
        ProfileItem(Profile p) { index = null; profile = p; }

        int dimension() { return profile.length(); }
        double getValue(int i) { return profile.value(i); }
    }
}
