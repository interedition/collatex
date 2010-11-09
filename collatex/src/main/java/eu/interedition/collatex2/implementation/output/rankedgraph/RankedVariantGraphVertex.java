package eu.interedition.collatex2.implementation.output.rankedgraph;

public class RankedVariantGraphVertex implements IRankedVariantGraphVertex {

  private final int rank;
  private final String normalized;

  public RankedVariantGraphVertex(int rank, String normalized) {
    this.rank = rank;
    this.normalized = normalized;
  }

  @Override
  public int getRank() {
    return rank;
  }

  @Override
  public String getNormalized() {
    return normalized;
  }

}
