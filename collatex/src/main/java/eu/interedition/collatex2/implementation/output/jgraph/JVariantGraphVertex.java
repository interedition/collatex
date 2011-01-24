package eu.interedition.collatex2.implementation.output.jgraph;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import eu.interedition.collatex2.interfaces.IJVariantGraphVertex;
import eu.interedition.collatex2.interfaces.IVariantGraphVertex;
import eu.interedition.collatex2.interfaces.IWitness;

public class JVariantGraphVertex implements IJVariantGraphVertex {
  private final StringBuilder normalized;
  private final Set<IWitness> witnesses;
  private final List<IVariantGraphVertex> variantGraphVertices;

  public JVariantGraphVertex(IVariantGraphVertex vgVertex) {
    normalized = new StringBuilder(vgVertex.getNormalized());
    witnesses = vgVertex.getWitnesses();
    variantGraphVertices = Lists.newArrayList(vgVertex);
  }

  public JVariantGraphVertex(String normalizedToken) {
    normalized = new StringBuilder(normalizedToken);
    witnesses = Sets.newHashSet();
    //NOTE: this could be dangerous! Where is this constructor called?
    variantGraphVertices = Lists.newArrayList();
  }

  @Override
  public void addVariantGraphVertex(IVariantGraphVertex nextVertex) {
    variantGraphVertices.add(nextVertex);
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

  @Override
  public List<IVariantGraphVertex> getVariantGraphVertices() {
    return variantGraphVertices;
  }
}
