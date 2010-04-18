package eu.interedition.collatex2.experimental.graph;

import java.util.Collection;

import eu.interedition.collatex2.interfaces.INormalizedToken;

public interface IAlignmentGraphIndex {

  boolean containsNormalizedPhrase(String normalized);

  Collection<INormalizedToken> getTokens(String normalized);

  IAlignmentNode getAlignmentNode(INormalizedToken token);
  
}
