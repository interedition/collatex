package eu.interedition.collatex2.experimental;

import eu.interedition.collatex2.interfaces.INormalizedToken;

public class AlignedToken implements IAlignedToken {

  private final INormalizedToken tokenB;

  public AlignedToken(INormalizedToken token, INormalizedToken tokenB) {
    this.tokenB = tokenB;
  }

  @Override
  public INormalizedToken getAlignedToken() {
    return tokenB;
  }

}
