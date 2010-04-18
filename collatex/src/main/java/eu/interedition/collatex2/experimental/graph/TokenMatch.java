package eu.interedition.collatex2.experimental.graph;

import eu.interedition.collatex2.interfaces.INormalizedToken;

public class TokenMatch implements ITokenMatch {

  private final INormalizedToken witnessToken;
  private final INormalizedToken matchingToken;

  public TokenMatch(INormalizedToken witnessToken, INormalizedToken matchingToken) {
    this.witnessToken = witnessToken;
    this.matchingToken = matchingToken;
  }
  
  @Override
  public String toString() {
    return witnessToken.getNormalized() + ": "+witnessToken.getPosition()+" -> "+matchingToken.getPosition();
  }

}
