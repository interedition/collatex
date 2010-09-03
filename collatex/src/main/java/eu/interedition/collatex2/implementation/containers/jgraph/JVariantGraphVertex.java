package eu.interedition.collatex2.implementation.containers.jgraph;

import eu.interedition.collatex2.interfaces.IJVariantGraphVertex;
import eu.interedition.collatex2.interfaces.IVariantGraphVertex;

public class JVariantGraphVertex implements IJVariantGraphVertex {
  private final StringBuilder normalized;

  public JVariantGraphVertex(IVariantGraphVertex startVertex) {
    normalized = new StringBuilder(startVertex.getNormalized());
  }

  public JVariantGraphVertex(String normalizedToken) {
    normalized = new StringBuilder(normalizedToken);
  }

  @Override
  public void addVariantGraphVertex(IVariantGraphVertex nextVertex) {
    normalized.append(" ").append(nextVertex.getNormalized());
  }

  @Override
  public String getNormalized() {
    return normalized.toString();
  }
}
