package eu.interedition.collatex.lab;

import edu.uci.ics.jung.graph.DirectedSparseGraph;
import eu.interedition.collatex.interfaces.INormalizedToken;

import java.util.Collections;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class VariantGraph extends DirectedSparseGraph<VariantGraphVertex, VariantGraphEdge> {

  private final VariantGraphVertex start;
  private final VariantGraphVertex end;

  public VariantGraph() {
    addVertex(start = new VariantGraphVertex(Collections.<INormalizedToken>emptyList()));
    addVertex(end = new VariantGraphVertex(Collections.<INormalizedToken>emptyList()));
  }

  public VariantGraphVertex getStart() {
    return start;
  }

  public VariantGraphVertex getEnd() {
    return end;
  }
}
