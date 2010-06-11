package eu.interedition.collatex2.experimental.graph;

import java.util.Set;

import eu.interedition.collatex2.interfaces.IWitness;

public interface IVariantGraphEdge {

  IVariantGraphVertex getBeginVertex();

  IVariantGraphVertex getEndVertex();

  //NOTE: unmodifiable set
  Set<IWitness> getWitnesses();
  
  void addWitness(IWitness witness);
  

}
