package eu.interedition.collatex2.experimental;

import eu.interedition.collatex2.interfaces.INormalizedToken;

public class TokenSequence implements ITokenSequence {

  private final INormalizedToken[] tokens;

  public TokenSequence(INormalizedToken... tokens) {
    this.tokens = tokens;
  }

  //NOTE: sloppy implemented!
  @Override
  public boolean equals(Object arg0) {
    TokenSequence other = (TokenSequence) arg0;
    if (tokens.length != other.tokens.length) {
      return false;
    }
    boolean theSame = true;
    for (int i = 0; i < tokens.length; i++ ) {
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
    StringBuilder bla = new StringBuilder("TokenSequence: ");
    for  (int i=0; i < tokens.length; i++) {
      INormalizedToken token = tokens[i];
      bla.append(token.toString()).append(", ");
    }
    
    return bla.toString();
  }

  @Override
  public INormalizedToken getFirstToken() {
    return tokens[0];
  }

  @Override
  public INormalizedToken getLastToken() {
    return tokens[tokens.length-1];
  }
}
