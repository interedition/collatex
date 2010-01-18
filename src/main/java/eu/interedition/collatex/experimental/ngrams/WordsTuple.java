package eu.interedition.collatex.experimental.ngrams;

import eu.interedition.collatex.input.Word;

public class WordsTuple {

  private final Word _previous;
  private final Word _next;

  public WordsTuple(final Word previous, final Word next) {
    this._previous = previous;
    this._next = next;
  }

  public String getNormalized() {
    return _previous.getNormalized() + " " + _next.getNormalized();
  }

  public Word getFirstWord() {
    return _previous;

  }

}
