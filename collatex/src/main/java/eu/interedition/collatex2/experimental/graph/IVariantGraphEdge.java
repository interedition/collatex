package eu.interedition.collatex2.experimental.graph;

import java.util.Set;

import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IWitness;

public interface IVariantGraphEdge {

  IVariantGraphNode getBeginNode();

  IVariantGraphNode getEndNode();

  //NOTE: unmodifiable set
  Set<IWitness> getWitnesses();
  
  INormalizedToken getToken(IWitness witness);
  
  void addToken(IWitness witness, INormalizedToken token);

}
