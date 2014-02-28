package eu.interedition.collatex.dekker.suffix;

import eu.interedition.collatex.simple.SimpleToken;

public class MarkerToken extends SimpleToken {
  private int witnessNumber;
  
  public MarkerToken(int witnessNumber) {
    super(null, "$", "$"+witnessNumber+"$");
    this.witnessNumber = witnessNumber;
  }

  @Override
  public String toString() {
    return getNormalized();
  }

}
