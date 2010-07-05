package eu.interedition.collatex2.input;

import eu.interedition.collatex2.interfaces.INormalizedToken;

public class NormalizedToken extends Token implements INormalizedToken {
  private String normalized;

  public NormalizedToken() {
    super();
  }

  public NormalizedToken(INormalizedToken other) {
    super(other);
    this.normalized = other.getNormalized();
  }

  public NormalizedToken(final String sigil, final String content, final int position, final String normalized) {
    super(sigil, content, position);
    this.normalized = normalized;
  }


  public String getNormalized() {
    return normalized;
  }

  public void setNormalized(String normalized) {
    this.normalized = normalized;
  }
}
