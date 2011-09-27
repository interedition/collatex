package eu.interedition.collatex2.implementation.edit_graph;

import org.jgrapht.experimental.dag.DirectedAcyclicGraph;

import eu.interedition.collatex2.implementation.vg_alignment.EndToken;
import eu.interedition.collatex2.interfaces.IVariantGraphVertex;

// we use a weighted DAG to make alignment decisions

@SuppressWarnings("serial")
public class EditGraph extends DirectedAcyclicGraph<EditGraphVertex, EditGraphEdge> {

  private final EditGraphVertex v1;
  private final EditGraphVertex end;

  public EditGraph(IVariantGraphVertex startVertex) {
    //TODO: that eight there is not handy!
    //TODO: the end vertex is unique by itself...
    //TODO: override the equals!
    this(new EditGraphVertex(null, startVertex), new EditGraphVertex(null, new EndToken(8)));
  }

  public EditGraph(EditGraphVertex startVertex, EditGraphVertex endVertex) {
	super(EditGraphEdge.class);
	this.v1 = startVertex;
	this.end = endVertex;
	add(startVertex, endVertex);
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
    return v1;
  }

  public EditGraphVertex getEndVertex() {
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

}
