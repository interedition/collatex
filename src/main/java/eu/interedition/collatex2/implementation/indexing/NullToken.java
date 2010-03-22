package eu.interedition.collatex2.implementation.indexing;

import eu.interedition.collatex2.interfaces.INormalizedToken;

public class NullToken implements INormalizedToken {
  private final int position;
  private final String sigil;

  public NullToken(final int position1, final String sigil1) {
    this.position = position1;
    this.sigil = sigil1;
  }

  @Override
  public String getNormalized() {
    return "#";
  }

  @Override
  public int getPosition() {
    return position;
  }

  @Override
  public String getSigil() {
    return sigil;
  }
}
