package eu.interedition.collatex2.implementation.matching;

import java.util.Set;

import eu.interedition.collatex2.interfaces.INormalizedToken;

public interface IMatchResult {

  Set<INormalizedToken> getUnmatchedTokens();

  Set<INormalizedToken> getUnsureTokens();
  
  Set<INormalizedToken> getSureTokens();

}
