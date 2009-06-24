package eu.interedition.collatex.collation;

import java.util.List;

import com.google.common.collect.Lists;

import eu.interedition.collatex.input.Witness;
import eu.interedition.collatex.input.Word;

public class Phrase {
  private final Witness witness;
  private final int startPosition;
  private final int endPosition;
  private final int size;
  private final Word previous;
  private final Word next;
  private final Match nextMatch;

  // TODO: It is pretty obvious: too many parameters here!
  public Phrase(Witness _witness, int _size, int _startPosition, int _endPosition, Word _previous, Word _next, Match _nextMatch) {
    witness = _witness;
    this.size = _size;
    this.next = _next;
    this.previous = _previous;
    startPosition = _startPosition;
    endPosition = _endPosition;
    this.nextMatch = _nextMatch;
  }

  public boolean hasGap() {
    return size > 0;
  }

  @Override
  public String toString() {
    List<String> words = Lists.newArrayList();
    for (int k = getStartPosition(); k <= getEndPosition(); k++) {
      String word = witness.getWordOnPosition(k).toString();
      words.add(word);
    }

    String replacementString = "";
    String divider = "";
    for (String replacement : words) {
      replacementString += divider + replacement;
      divider = " ";
    }
    return replacementString;
  }

  public Witness getWitness() {
    return witness;
  }

  public int getStartPosition() {
    return startPosition;
  }

  public int getEndPosition() {
    return endPosition;
  }

  public List<Word> getWords() {
    List<Word> words = Lists.newArrayList();
    for (int k = getStartPosition(); k <= getEndPosition(); k++) {
      Word word = getWitness().getWordOnPosition(k);
      words.add(word);
    }
    return words;
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

  public boolean isAtTheEnd() {
    return next == null;
  }

  public Word getPreviousWord() {
    if (isAtTheFront()) {
      throw new RuntimeException("There is no previous word!");
    }
    return previous;
  }

  public boolean isAtTheFront() {
    return previous == null;
  }

  // TODO: either getNextWord should be removed or getNextMatch should be removed!
  public Match getNextMatch() {
    if (isAtTheEnd()) {
      throw new RuntimeException("There is no next match!");
    }
    return nextMatch;
  }

}
