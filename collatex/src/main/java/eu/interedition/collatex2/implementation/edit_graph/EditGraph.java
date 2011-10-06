package eu.interedition.collatex2.implementation.edit_graph;

import org.jgrapht.experimental.dag.DirectedAcyclicGraph;

// This class is the container class for the Edit Graph
// This is a mutable class that is constructed by an external
// class, since the construction process is an elaborate one.
// This class is implemented in a defensive style
// We use a weighted DAG to make alignment decisions

@SuppressWarnings("serial")
public class EditGraph extends DirectedAcyclicGraph<EditGraphVertex, EditGraphEdge> {
  private EditGraphVertex start;
  private EditGraphVertex end;

  public EditGraph() {
    super(EditGraphEdge.class);
  }

  public void add(EditGraphVertex... vertices) {
    for (EditGraphVertex v : vertices) {
      addVertex(v);
    }
  }

  public void add(EditGraphEdge... edges) {
    for (EditGraphEdge e : edges) {
      addEdge(e.getSourceVertex(), e.getTargetVertex(), e);
    }
  }

  public EditGraphVertex getStartVertex() {
    if (start==null) {
      throw new RuntimeException("Start vertex of Edit Graph is not set!");
    }
    return start;
  }

  public EditGraphVertex getEndVertex() {
    if (end==null) {
      throw new RuntimeException("End vertex of Edit Graph is not set!");
    }
    return end;
  }

  public EditGraphEdge edge(EditGraphVertex source, EditGraphVertex target) {
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
  
  public void setStartVertex(EditGraphVertex startVertex) {
    this.start = startVertex;
    addVertex(startVertex);
  }

  public void setEndVertex(EditGraphVertex endVertex) {
    this.end = endVertex;
    addVertex(endVertex);
  }
}
