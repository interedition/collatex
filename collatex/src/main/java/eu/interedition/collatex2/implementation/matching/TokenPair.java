/**
 * 
 */
package eu.interedition.collatex2.implementation.matching;

import eu.interedition.collatex2.interfaces.INormalizedToken;

public class TokenPair {
  INormalizedToken tableToken;
  INormalizedToken witnessToken;

  public TokenPair(INormalizedToken tableToken, INormalizedToken witnessToken) {
    this.tableToken = tableToken;
    this.witnessToken = witnessToken;
  }
}