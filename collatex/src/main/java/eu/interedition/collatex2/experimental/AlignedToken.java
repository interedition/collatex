package eu.interedition.collatex2.experimental;

import eu.interedition.collatex2.interfaces.INormalizedToken;

public class AlignedToken implements IAlignedToken {

  private final INormalizedToken tokenB;
  private final INormalizedToken tokenA;

  public AlignedToken(INormalizedToken token, INormalizedToken tokenB) {
    this.tokenA = token;
    this.tokenB = tokenB;
  }

  @Override
  public INormalizedToken getAlignedToken() {
    return tokenB;
  }

  @Override
  public INormalizedToken getWitnessToken() {
    return tokenA;
  }
}
