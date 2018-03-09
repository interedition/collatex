package eu.interedition.collatex.dekker.pairwise_merge_graph;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/*
 * 8 March 2018
 * Ronald Haentjens Dekker
 *
 * A undirected cyclic generic graph class.
 * We work with actual tokens as vertices
 */
public class AlignmentMergeGraph<V> {
    private Set<V> vertices = new LinkedHashSet<>();
    private Map<V, V> edges = new HashMap<>();


    public void addVertex(V v) {
        vertices.add(v);
    }
}
