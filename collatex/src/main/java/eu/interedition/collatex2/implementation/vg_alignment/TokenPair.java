package eu.interedition.collatex2.implementation.vg_alignment;

import eu.interedition.collatex2.interfaces.INormalizedToken;

public class TokenPair {

  public final INormalizedToken tableToken;
  public final INormalizedToken witnessToken;

  public TokenPair(INormalizedToken tableToken, INormalizedToken witnessToken) {
    this.tableToken = tableToken;
    this.witnessToken = witnessToken;
  }

}
