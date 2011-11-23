package eu.interedition.collatex2.implementation.vg_alignment;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;

import eu.interedition.collatex2.interfaces.INormalizedToken;

public class TokenSequence implements ITokenSequence {

  private final List<INormalizedToken> tokens;
  private final boolean expandsToTheRight;
  
  public TokenSequence(boolean expandsToTheRight, List<INormalizedToken> tokens) {
    this.tokens = Collections.unmodifiableList(tokens);
    this.expandsToTheRight = expandsToTheRight;
  }

  public TokenSequence(boolean expandsToTheRight, INormalizedToken... tokens) {
    this(expandsToTheRight, Arrays.asList(tokens));
  }

  @Override
  public INormalizedToken getFirstToken() {
    return tokens.get(0);
  }

  @Override
  public INormalizedToken getLastToken() {
    return tokens.get(tokens.size() - 1);
  }
  
  public List<INormalizedToken> getTokens() {
    return tokens;
  }
  
  @Override
  public boolean expandsToTheRight() {
    return expandsToTheRight;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj != null && obj instanceof TokenSequence) {
      return tokens.equals(((TokenSequence)obj).tokens);
    }
    return super.equals(obj);
  }

  @Override
  public int hashCode() {
    return tokens.hashCode();
  }

  @Override
  public String toString() {
    return TokenLinker.toString(getTokens());
  }
}
