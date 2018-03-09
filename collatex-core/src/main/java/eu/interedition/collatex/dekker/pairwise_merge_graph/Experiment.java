package eu.interedition.collatex.dekker.pairwise_merge_graph;

import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.dekker.editgraphaligner.EditGraphAligner;
import eu.interedition.collatex.simple.SimpleWitness;

import java.util.*;

/*
 * 8 March 2018
 * Ronald Haentjens Dekker
 */
public class Experiment {

    public static void main(String[] args) {
        // First Witness set
        // 1: a, b, c, d, e
        // 2: a, e, c, d
        // 3: a, d, b

        SimpleWitness w1 = new SimpleWitness("1", "a b c d e");
        SimpleWitness w2 = new SimpleWitness("2", "a e c d");

        List<SimpleWitness> witnesses = new ArrayList<>();
        witnesses.add(w1);
        witnesses.add(w2);

        VariantGraph graph = new VariantGraph();

        // first we align witness 1 and 2
        EditGraphAligner aligner = new EditGraphAligner();
        aligner.collate(graph, witnesses);

       // Nu willen we de vertices van alignment omzetten naar twee vertices in de merge graph
        Iterable<VariantGraph.Vertex> vertices = graph.vertices();
        Iterator<VariantGraph.Vertex> vertexIterator  = vertices.iterator();

        // We filteren alle vertices eruit waar er twee tokens op de vertex zitten
        Set<VariantGraph.Vertex> alignedVertices = new LinkedHashSet<>();
        while (vertexIterator.hasNext()) {
            VariantGraph.Vertex v = vertexIterator.next();
            if (v.tokens().size() > 1) {
                alignedVertices.add(v);
            }
        }
        System.out.println(alignedVertices);
    }

}
