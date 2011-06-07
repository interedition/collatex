package eu.interedition.collatex2.experimental;

import java.util.List;

import com.google.common.collect.Lists;

import eu.interedition.collatex2.interfaces.INormalizedToken;

public class TokenSequence implements ITokenSequence {

  private final INormalizedToken[] tokens;
  private final boolean gotoleft;
  
  public TokenSequence(boolean gotoleft, INormalizedToken... tokens) {
    this.tokens = tokens;
    this.gotoleft = gotoleft;
  }

  public TokenSequence(List<INormalizedToken> tokens2, boolean gotoleft) {
    this.tokens = tokens2.toArray(new INormalizedToken[tokens2.size()]);
    this.gotoleft = gotoleft;
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
    StringBuilder buffer = new StringBuilder("TokenSequence: ");
    for  (int i=0; i < tokens.length; i++) {
      INormalizedToken token = tokens[i];
      if (token==null) {
        throw new RuntimeException("token is null");
      } 
      buffer.append(token.toString()).append(", ");
    }
    return buffer.toString();
  }

  @Override
  public INormalizedToken getFirstToken() {
    return tokens[0];
  }

  @Override
  public INormalizedToken getLastToken() {
    return tokens[tokens.length-1];
  }
  
  public List<INormalizedToken> getTokens() {
    return Lists.newArrayList(tokens);
  }
  
  //Note: just for testing purposes
  public String getNormalized() {
    //hmm: I have written this code before!
    String separator = "";
    StringBuffer buffer = new StringBuffer();
    for (INormalizedToken token : getTokens()) {
      buffer.append(separator);
      buffer.append(token.getNormalized());
      separator = " ";
    }
    return buffer.toString();
  }
  
  @Override
  public boolean expandsToTheRight() {
    return gotoleft;
  }
}
