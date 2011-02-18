package eu.interedition.collatex2.implementation.input;

import eu.interedition.collatex2.interfaces.INormalizedToken;

public class NullToken implements INormalizedToken {
  private final String sigil;

  public NullToken(final String sigil1) {
    this.sigil = sigil1;
  }

  @Override
  public String getNormalized() {
    return "#";
  }

  @Override
  public String getSigil() {
    return sigil;
  }

  @Override
  public String getContent() {
    return "";
  }
}
