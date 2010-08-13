package eu.interedition.collatex2.experimental.tokenmatching;

import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IToken;
import eu.interedition.collatex2.interfaces.ITokenMatch;

public class TokenMatch implements ITokenMatch {
  //TODO: remove duplication here! I look witnessToken and matchingToken the best!
  private final INormalizedToken tableToken;
  private final INormalizedToken token;

  public TokenMatch(INormalizedToken tableToken, INormalizedToken token) {
    this.tableToken = tableToken;
    this.token = token; //TODO: rename!
  }

  @Override
  public String getNormalized() {
    return token.getNormalized();
  }

  @Override
  public INormalizedToken getTableToken() {
    return tableToken;
  }

  @Override
  public INormalizedToken getWitnessToken() {
    return token;
  }
  
  private IToken getMatchingToken() {
    return tableToken;
  }

  @Override
  public INormalizedToken getTokenA() {
    return token;
  }

  @Override
  public INormalizedToken getTokenB() {
    return tableToken;
  }

  @Override
  public String toString() {
    return getNormalized() + ": "+getWitnessToken().getPosition()+" -> "+getMatchingToken().getPosition();
  }
}
