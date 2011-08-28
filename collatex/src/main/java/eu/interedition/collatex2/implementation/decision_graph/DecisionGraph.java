package eu.interedition.collatex2.implementation.decision_graph;

import org.jgrapht.experimental.dag.DirectedAcyclicGraph;

import eu.interedition.collatex2.implementation.vg_alignment.EndToken;
import eu.interedition.collatex2.interfaces.IVariantGraphVertex;

// we use a weighted DAG to make alignment decisions

@SuppressWarnings("serial")
public class DecisionGraph extends DirectedAcyclicGraph<DGVertex, DGEdge> {

  private final DGVertex v1;
  private final DGVertex end;

  public DecisionGraph(IVariantGraphVertex startVertex) {
    //TODO: that eight there is not handy!
    //TODO: the end vertex is unique by itself...
    //TODO: override the equals!
    this(new DGVertex(startVertex), new DGVertex(new EndToken(8)));
  }

  public DecisionGraph(DGVertex startVertex, DGVertex endVertex) {
	super(DGEdge.class);
	this.v1 = startVertex;
	this.end = endVertex;
	add(startVertex, endVertex);
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

  public DGEdge edge(DGVertex source, DGVertex target) {
    if (!this.containsVertex(source)) {
      throw new RuntimeException("Source vertex does not exist in the graph!");
    }
    if (!this.containsVertex(target)) {
      throw new RuntimeException("Target vertex does not exist in the graph!");
    }
    if (!this.containsEdge(source, target)) {
      throw new RuntimeException("Edge does not exist in the graph! Vertices do exist!");
    }
    return this.getEdge(source, target);
  }

}
