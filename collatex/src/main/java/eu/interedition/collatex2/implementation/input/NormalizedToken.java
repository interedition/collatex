package eu.interedition.collatex2.implementation.input;

import eu.interedition.collatex2.interfaces.INormalizedToken;

public class NormalizedToken extends Token implements INormalizedToken {
  private final String normalized;

  public NormalizedToken(final String sigil, final String content, final int position, final String normalized) {
    super(sigil, content, position);
    this.normalized = normalized;
  }

  public String getNormalized() {
    return normalized;
  }
}
