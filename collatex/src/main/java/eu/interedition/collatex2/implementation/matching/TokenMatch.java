package eu.interedition.collatex2.implementation.matching;

import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.ITokenMatch;

public class TokenMatch implements ITokenMatch {

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
  
  @Override
  public String toString() {
    return token.getContent()+" -> "+tableToken.getContent();
  }


}
