package eu.interedition.collatex2.experimental;

import eu.interedition.collatex2.implementation.containers.witness.WitnessToken;

public class EndToken extends WitnessToken {

  public EndToken(int position) {
    super("", position, "#");
  }
}
