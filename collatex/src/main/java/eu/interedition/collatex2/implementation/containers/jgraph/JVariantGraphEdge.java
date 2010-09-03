package eu.interedition.collatex2.implementation.containers.jgraph;

import java.util.Set;

import eu.interedition.collatex2.interfaces.IJVariantGraphEdge;
import eu.interedition.collatex2.interfaces.IVariantGraphEdge;
import eu.interedition.collatex2.interfaces.IWitness;

public class JVariantGraphEdge implements IJVariantGraphEdge {
  private final IVariantGraphEdge vertex;

  public JVariantGraphEdge(IVariantGraphEdge vertex) {
    this.vertex = vertex;
  }

  @Override
  public Set<IWitness> getWitnesses() {
    return vertex.getWitnesses();
  }

}
