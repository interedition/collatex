package eu.interedition.collatex2.implementation.decision_graph;

import org.jgrapht.experimental.dag.DirectedAcyclicGraph;

@SuppressWarnings("serial")
public class DecisionGraph extends DirectedAcyclicGraph<DGVertex, DGEdge> {

  public DecisionGraph() {
    super(DGEdge.class);
  }

  public void add(DGVertex... vertices) {
    for (DGVertex v : vertices) {
      addVertex(v);
    }
  }

  public void add(DGEdge... edges) {
    for (DGEdge e : edges) {
      addEdge(e.getBeginVertex(), e.getEndVertex(), e);
    }
  }

}
