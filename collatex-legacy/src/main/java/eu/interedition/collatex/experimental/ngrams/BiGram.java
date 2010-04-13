package eu.interedition.collatex.experimental.ngrams;

import eu.interedition.collatex.experimental.ngrams.data.NormalizedToken;
import eu.interedition.collatex.experimental.ngrams.data.Token;
import eu.interedition.collatex2.interfaces.INormalizedToken;

public class BiGram {
  // NOTE: BiGram could become an extend version of NormalizedToken!
  private final INormalizedToken _previous;
  private final INormalizedToken _next;

  public BiGram(final INormalizedToken previous, final INormalizedToken next) {
    this._previous = previous;
    this._next = next;
  }

  public String getNormalized() {
    return _previous.getNormalized() + " " + _next.getNormalized();
  }

  public INormalizedToken getFirstToken() {
    return _previous;

  }

  public INormalizedToken getLastToken() {
    return _next;
  }

  public static BiGram create(final Token token, final Token token2) {
    return new BiGram(NormalizedToken.normalize(token), NormalizedToken.normalize(token2));
  }

  public boolean contains(final Token token) {
    return getFirstToken().equals(token) || getLastToken().equals(token);
  }
}
