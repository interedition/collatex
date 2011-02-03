package eu.interedition.collatex2.implementation.output.rankedgraph;

import eu.interedition.collatex2.implementation.output.segmented_graph.ISegmentedVariantGraphVertex;

public class RankedVariantGraphVertex implements IRankedVariantGraphVertex {

  private final int rank;
  private final ISegmentedVariantGraphVertex vertex;

  public RankedVariantGraphVertex(int rank, ISegmentedVariantGraphVertex vertex) {
    this.rank = rank;
    this.vertex = vertex;
  }

  @Override
  public int getRank() {
    return rank;
  }

  @Override
  public ISegmentedVariantGraphVertex getVertex() {
    return vertex;
  }

}
