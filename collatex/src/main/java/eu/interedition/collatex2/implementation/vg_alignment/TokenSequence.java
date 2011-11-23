package eu.interedition.collatex2.implementation.vg_alignment;

import java.util.List;

import com.google.common.collect.Lists;

import eu.interedition.collatex2.interfaces.INormalizedToken;

public class TokenSequence implements ITokenSequence {

  private final INormalizedToken[] tokens;
  private final boolean expandsToTheRight;

  public TokenSequence(boolean expandsToTheRight, INormalizedToken... tokens) {
    this.tokens = tokens;
    this.expandsToTheRight = expandsToTheRight;
  }

  public TokenSequence(List<INormalizedToken> tokens2, boolean expandsToTheRight) {
    this.tokens = tokens2.toArray(new INormalizedToken[tokens2.size()]);
    this.expandsToTheRight = expandsToTheRight;
  }

  //NOTE: sloppy implemented!
  @Override
  public boolean equals(Object arg0) {
    TokenSequence other = (TokenSequence) arg0;
    if (tokens.length != other.tokens.length) {
      return false;
    }
    boolean theSame = true;
    for (int i = 0; i < tokens.length; i++) {
      INormalizedToken tokenA = tokens[i];
      INormalizedToken tokenB = other.tokens[i];
      theSame = theSame && (tokenA == tokenB);
    }
    return theSame;
  }

  @Override
  public int hashCode() {
    return 69 * tokens.length;
  }

  @Override
  public String toString() {
    return TokenLinker.toString(getTokens());
  }

  @Override
  public INormalizedToken getFirstToken() {
    return tokens[0];
  }

  @Override
  public INormalizedToken getLastToken() {
    return tokens[tokens.length - 1];
  }

  @Override
  public List<INormalizedToken> getTokens() {
    return Lists.newArrayList(tokens);
  }

  @Override
  public boolean expandsToTheRight() {
    return expandsToTheRight;
  }
}
