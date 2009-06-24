package eu.interedition.collatex.collation;

import eu.interedition.collatex.input.Witness;
import eu.interedition.collatex.input.Word;

public class Gap extends Phrase {
  private final Word previous;
  private final Word next;
  private final Match nextMatch;

  public Gap(Witness _witness, int _size, int _beginPosition, int _endPosition, Word _previous, Word _next, Match _nextMatch) {
    super(_witness, _size, _beginPosition, _endPosition);
    this.previous = _previous;
    this.next = _next;
    this.nextMatch = _nextMatch;
  }

  public Word getFirstWord() {
    return getWords().get(0);
  }

  public Word getNextWord() {
    if (isAtTheEnd()) {
      throw new RuntimeException("There is no next word!");
    }
    return next;
  }

  public Word getPreviousWord() {
    if (isAtTheFront()) {
      throw new RuntimeException("There is no previous word!");
    }
    return previous;
  }

  public boolean isAtTheEnd() {
    return next == null;
  }

  public boolean isAtTheFront() {
    return previous == null;
  }

  public Match getNextMatch() {
    if (isAtTheEnd()) {
      throw new RuntimeException("There is no next match!");
    }
    return nextMatch;
  }

}
