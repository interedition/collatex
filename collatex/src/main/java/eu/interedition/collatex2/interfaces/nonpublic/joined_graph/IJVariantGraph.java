package eu.interedition.collatex2.interfaces.nonpublic.joined_graph;

import org.jgrapht.DirectedGraph;

public interface IJVariantGraph extends DirectedGraph<IJVariantGraphVertex, IJVariantGraphEdge> {

  IJVariantGraphVertex getStartVertex();

  void setStartVertex(IJVariantGraphVertex startVertex);

  IJVariantGraphVertex getEndVertex();

  void setEndVertex(IJVariantGraphVertex endVertex);

}
