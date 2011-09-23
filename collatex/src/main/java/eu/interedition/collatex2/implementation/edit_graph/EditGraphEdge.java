package eu.interedition.collatex2.implementation.edit_graph;

public class EditGraphEdge {

  private final EditGraphVertex v1;
  private final EditGraphVertex v2;
  private final int weight;

  public EditGraphEdge(EditGraphVertex v1, EditGraphVertex v2, int weight) {
    this.v1 = v1;
    this.v2 = v2;
    this.weight = weight;
  }

  public EditGraphVertex getBeginVertex() {
    return v1;
  }

  public EditGraphVertex getTargetVertex() {
    return v2;
  }

  public Integer getWeight() {
    return weight;
  }

}
