package eu.interedition.collatex2.implementation.containers.jgraph;

import org.jgrapht.experimental.dag.DirectedAcyclicGraph;

import eu.interedition.collatex2.interfaces.IJVariantGraph;
import eu.interedition.collatex2.interfaces.IJVariantGraphEdge;
import eu.interedition.collatex2.interfaces.IJVariantGraphVertex;

public class JVariantGraph extends DirectedAcyclicGraph<IJVariantGraphVertex, IJVariantGraphEdge> implements IJVariantGraph {
  private static final long serialVersionUID = 1L;
  private final IJVariantGraphVertex startVertex;
  private final JVariantGraphVertex endVertex;

  public JVariantGraph() {
    super(IJVariantGraphEdge.class);
    startVertex = new JVariantGraphVertex("#");
    addVertex(startVertex);
    endVertex = new JVariantGraphVertex("#");
    addVertex(getEndVertex());
  }

  public static IJVariantGraph create() {
    return new JVariantGraph();
  }

  @Override
  public IJVariantGraphVertex getStartVertex() {
    return startVertex;
  }

  public IJVariantGraphVertex getEndVertex() {
    return endVertex;
  }

}
