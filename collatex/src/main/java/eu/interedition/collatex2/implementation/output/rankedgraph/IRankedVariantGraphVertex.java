package eu.interedition.collatex2.implementation.output.rankedgraph;

import eu.interedition.collatex2.implementation.output.segmented_graph.ISegmentedVariantGraphVertex;

public interface IRankedVariantGraphVertex {

  int getRank();

  ISegmentedVariantGraphVertex getVertex();

}
