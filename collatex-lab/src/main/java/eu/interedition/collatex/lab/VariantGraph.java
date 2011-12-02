package eu.interedition.collatex.lab;

import edu.uci.ics.jung.graph.DirectedSparseGraph;
import eu.interedition.collatex.interfaces.INormalizedToken;

import java.util.Collections;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class VariantGraph extends DirectedSparseGraph<VariantGraphVertex, VariantGraphEdge> {

  private VariantGraphVertex start;
  private VariantGraphVertex end;

  public VariantGraphVertex getStart() {
    return start;
  }

  public void setStart(VariantGraphVertex start) {
    this.start = start;
  }

  public VariantGraphVertex getEnd() {
    return end;
  }

  public void setEnd(VariantGraphVertex end) {
    this.end = end;
  }
}
