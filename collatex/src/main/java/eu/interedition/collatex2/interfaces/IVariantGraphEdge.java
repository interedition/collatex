package eu.interedition.collatex2.interfaces;

import java.util.Set;

public interface IVariantGraphEdge {

  // NOTE: unmodifiable set
  Set<IWitness> getWitnesses();

  void addWitness(IWitness witness);

  boolean containsWitness(IWitness witness);

}
