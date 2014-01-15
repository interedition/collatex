package eu.interedition.collatex.dekker.decision_tree2;

import com.google.common.collect.Sets;

import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.VariantGraph.Vertex;
import eu.interedition.collatex.Witness;

/*
 * Utility class to build easily build variant graphs
 *
 * @author: Ronald Haentjens Dekker
 *
 */
public class VariantGraphBuilder {
  // Note: method looks like CollationAlgorithm.Base merge (can't use that
  // method
  // due to inheritance)
  public static void addFirstWitnessToGraph(VariantGraph graph, Iterable<Token> witness) {
    Vertex from = graph.getStart();
    for (Token token : witness) {
      Vertex to = graph.add(token);
      graph.connect(from, to, Sets.newHashSet((Witness) witness));
      from = to;
    }
    graph.connect(from, graph.getEnd(), Sets.newHashSet((Witness) witness));
  }
}
