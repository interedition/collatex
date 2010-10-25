package eu.interedition.collatex2.legacy.indexing;

import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IToken;

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

  @Override
  public String getContent() {
    return "";
  }

  @Override
  public boolean isNear(IToken b) {
    throw new RuntimeException("THIS METHOD SHOULD NOT BE CALLED!");
  }
}
