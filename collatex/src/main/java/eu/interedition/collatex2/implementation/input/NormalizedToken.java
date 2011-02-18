package eu.interedition.collatex2.implementation.input;

import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IToken;

public class NormalizedToken extends Token implements INormalizedToken {
  protected String normalized;

  public NormalizedToken(IToken token, String _normalized) {
    super(token);
    this.normalized = _normalized;
  }

  @Override
  public String getNormalized() {
    return normalized;
  }

  public void setNormalized(String normalized) {
    this.normalized = normalized;
  }
}
