package eu.interedition.collatex2.experimental;

import java.util.Set;

import eu.interedition.collatex2.interfaces.INormalizedToken;

public class MatchResult implements IMatchResult {

  private final Set<INormalizedToken> unmatchedTokens;
  private final Set<INormalizedToken> unsureTokens;
  private final Set<INormalizedToken> sureTokens;

  public MatchResult(Set<INormalizedToken> unmatchedTokens, Set<INormalizedToken> unsureTokens, Set<INormalizedToken> sureTokens) {
    this.unmatchedTokens = unmatchedTokens;
    this.unsureTokens = unsureTokens;
    this.sureTokens = sureTokens;
  }

  @Override
  public Set<INormalizedToken> getUnmatchedTokens() {
    return unmatchedTokens;
  }

  @Override
  public Set<INormalizedToken> getUnsureTokens() {
    return unsureTokens;
  }

  @Override
  public Set<INormalizedToken> getSureTokens() {
    return sureTokens;
  }

}
