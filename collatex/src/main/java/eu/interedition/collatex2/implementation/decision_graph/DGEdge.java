package eu.interedition.collatex2.implementation.decision_graph;

public class DGEdge {

  private final DGVertex v1;
  private final DGVertex v2;
  private final int weight;

  public DGEdge(DGVertex v1, DGVertex v2, int weight) {
    this.v1 = v1;
    this.v2 = v2;
    this.weight = weight;
  }

  public DGVertex getBeginVertex() {
    return v1;
  }

  public DGVertex getEndVertex() {
    return v2;
  }

  public Integer getWeight() {
    return weight;
  }

}
