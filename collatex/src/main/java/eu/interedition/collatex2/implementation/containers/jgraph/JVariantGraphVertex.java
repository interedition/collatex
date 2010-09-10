package eu.interedition.collatex2.implementation.containers.jgraph;

import java.util.Set;

import com.google.common.collect.Sets;

import eu.interedition.collatex2.interfaces.IJVariantGraphVertex;
import eu.interedition.collatex2.interfaces.IVariantGraphVertex;
import eu.interedition.collatex2.interfaces.IWitness;

public class JVariantGraphVertex implements IJVariantGraphVertex {
  private final StringBuilder normalized;
  private final Set<IWitness> witnesses;

  public JVariantGraphVertex(IVariantGraphVertex vgVertex) {
    normalized = new StringBuilder(vgVertex.getNormalized());
    witnesses = vgVertex.getWitnesses();
  }

  public JVariantGraphVertex(String normalizedToken) {
    normalized = new StringBuilder(normalizedToken);
    witnesses = Sets.newHashSet();
  }

  @Override
  public void addVariantGraphVertex(IVariantGraphVertex nextVertex) {
    normalized.append(" ").append(nextVertex.getNormalized());
  }

  @Override
  public String getNormalized() {
    return normalized.toString();
  }

  @Override
  public Set<IWitness> getWitnesses() {
    return witnesses;
  }

  @Override
  public String toString() {
    return "{" + getNormalized() + "}";
  }
}
