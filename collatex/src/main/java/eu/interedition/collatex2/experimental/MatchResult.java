package eu.interedition.collatex2.experimental;

import java.util.Set;

import eu.interedition.collatex2.interfaces.INormalizedToken;

public class MatchResult implements IMatchResult {

  private final Set<INormalizedToken> unmatchedTokens;
  private final Set<INormalizedToken> unsureTokens;

  public MatchResult(Set<INormalizedToken> unmatchedTokens, Set<INormalizedToken> unsureTokens) {
    this.unmatchedTokens = unmatchedTokens;
    this.unsureTokens = unsureTokens;
  }

  @Override
  public Set<INormalizedToken> getUnmatchedTokens() {
    return unmatchedTokens;
  }

  @Override
  public Set<INormalizedToken> getUnsureTokens() {
    return unsureTokens;
  }

}
