package eu.interedition.collatex2.experimental.graph;

import java.util.Collection;

import eu.interedition.collatex2.interfaces.INormalizedToken;

public interface IVariantGraphIndex {

  boolean containsNormalizedPhrase(String normalized);

  Collection<INormalizedToken> getTokens(String normalized);

  IVariantGraphNode getAlignmentNode(INormalizedToken token);
  
}
