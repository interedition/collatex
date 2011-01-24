package eu.interedition.collatex2.interfaces;

import java.util.Set;

public interface IVariantGraphEdge {

  // TODO: should this method be here? Better to just ask the graph!
  // otherwise: make return type generic!
  IVariantGraphVertex getBeginVertex();

  // TODO: should this method be here? Better to just ask the graph!
  // otherwise: make return type generic!
  IVariantGraphVertex getEndVertex();

  // NOTE: unmodifiable set
  Set<IWitness> getWitnesses();

  void addWitness(IWitness witness);

  boolean containsWitness(IWitness witness);

}
