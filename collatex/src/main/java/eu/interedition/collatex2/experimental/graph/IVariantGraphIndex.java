package eu.interedition.collatex2.experimental.graph;

import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IWitnessIndex;

public interface IVariantGraphIndex extends IWitnessIndex {

  boolean containsNormalizedPhrase(String normalized);

  IVariantGraphVertex getVertex(INormalizedToken token);
  
}
