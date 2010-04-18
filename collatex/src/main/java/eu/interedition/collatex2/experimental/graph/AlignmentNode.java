package eu.interedition.collatex2.experimental.graph;

public class AlignmentNode implements IAlignmentNode {

  private final String normalized;

  public AlignmentNode(String normalized) {
    this.normalized = normalized;
  }

  @Override
  public String getNormalized() {
    return normalized;
  }

}
