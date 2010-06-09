package eu.interedition.collatex2.experimental.graph;

import java.util.Collection;

import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IWitnessIndex;

public interface IVariantGraphIndex extends IWitnessIndex {

  boolean containsNormalizedPhrase(String normalized);

  Collection<INormalizedToken> getTokens(String normalized);

  IVariantGraphVertex getAlignmentNode(INormalizedToken token);
  
}
