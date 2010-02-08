package eu.interedition.collatex.experimental.ngrams.data;

public class BiGram {
  // NOTE: BiGram could become an extend version of NormalizedToken!
  private final NormalizedToken _previous;
  private final NormalizedToken _next;

  public BiGram(final NormalizedToken previous, final NormalizedToken next) {
    this._previous = previous;
    this._next = next;
  }

  public String getNormalized() {
    return _previous.getNormalized() + " " + _next.getNormalized();
  }

  public NormalizedToken getFirstToken() {
    return _previous;

  }

}
