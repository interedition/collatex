package eu.interedition.collatex.alignment;

import java.util.List;

import com.google.common.collect.Lists;

import eu.interedition.collatex.input.BaseElement;
import eu.interedition.collatex.input.Segment;
import eu.interedition.collatex.input.Word;

public class Phrase<T extends BaseElement> {
  private final Segment witness;
  private final int startPosition;
  private final int endPosition;
  private final int size;
  private final T previous;
  private final T next;

  // TODO: It is pretty obvious: too many parameters here!
  // Note: probably two constructors needed...
  // Note: one where the phrase resembles the words between two other words of the witness
  // Note: one where the start and end words of the phrase are given

  public Phrase(Segment _witness, int _size, int _startPosition, int _endPosition, T _previous, T _next) {
    witness = _witness;
    this.size = _size;
    this.next = _next;
    this.previous = _previous;
    startPosition = _startPosition;
    endPosition = _endPosition;
  }

  public Phrase(Segment _witness, Word beginWord, Word endWord) {
    this.witness = _witness;
    this.size = -1; // !!!
    this.next = null; // !!!
    this.previous = null; // !!!
    this.startPosition = beginWord.position;
    this.endPosition = endWord.position;
  }

  //TODO: rename method!
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

  public Segment getWitness() {
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

  public T getNextWord() {
    if (isAtTheEnd()) {
      throw new RuntimeException("There is no next word!");
    }
    return next;
  }

  public boolean isAtTheEnd() {
    return next == null;
  }

  public T getPreviousWord() {
    if (isAtTheFront()) {
      throw new RuntimeException("There is no previous word!");
    }
    return previous;
  }

  public boolean isAtTheFront() {
    return previous == null;
  }
}
