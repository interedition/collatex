package eu.interedition.collatex2.implementation.decision_graph;

import org.jgrapht.experimental.dag.DirectedAcyclicGraph;

import eu.interedition.collatex2.implementation.vg_alignment.EndToken;
import eu.interedition.collatex2.implementation.vg_alignment.StartToken;

@SuppressWarnings("serial")
public class DecisionGraph extends DirectedAcyclicGraph<DGVertex, DGEdge> {

  private final DGVertex v1;
  private final DGVertex end;

  public DecisionGraph() {
    super(DGEdge.class);
    v1 = new DGVertex(new StartToken());
    //TODO: that eight there is not handy!
    //TODO: the end vertex is unique by itself...
    //TODO: override the equals!
    end = new DGVertex(new EndToken(8));
    addVertex(v1);
    addVertex(end);
  }

  public void add(DGVertex... vertices) {
    for (DGVertex v : vertices) {
      addVertex(v);
    }
  }

  public void add(DGEdge... edges) {
    for (DGEdge e : edges) {
      addEdge(e.getBeginVertex(), e.getTargetVertex(), e);
    }
  }

  public DGVertex getStartVertex() {
    return v1;
  }

  public DGVertex getEndVertex() {
    return end;
  }

}
