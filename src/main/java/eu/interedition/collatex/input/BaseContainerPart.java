package eu.interedition.collatex.input;

import java.util.List;

import com.google.common.collect.Lists;

// Note: this class started life as a duplicate of
// Phase; it will get a life of its own
// for instance...
// it might not need to extend BaseElement?
// anyway it should take BaseElements in
public class BaseContainerPart<T extends BaseElement> extends BaseElement {
  private final BaseContainer<T> _witness;
  private final int startPosition;
  private final int endPosition;
  private final int size;
  private final T previous;
  private final T next;

  // TODO: It is pretty obvious: too many parameters here!
  // Note: probably two constructors needed...
  // Note: one where the phrase resembles the words between two other words of the witness
  // Note: one where the start and end words of the phrase are given

  public BaseContainerPart(final BaseContainer<T> witness, final int _size, final int _startPosition, final int _endPosition, final T _previous, final T _next) {
    this._witness = witness;
    this.size = _size;
    this.next = _next;
    this.previous = _previous;
    startPosition = _startPosition;
    endPosition = _endPosition;
  }

  public BaseContainerPart(final BaseContainer<T> witness, final Word beginWord, final Word endWord) {
    this._witness = witness;
    this.size = endWord.position - beginWord.position + 1;
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
    final List<String> words = Lists.newArrayList();
    for (int k = getBeginPosition(); k <= getEndPosition(); k++) {
      final String word = _witness.getWordOnPosition(k).toString();
      words.add(word);
    }

    String replacementString = "";
    String divider = "";
    for (final String replacement : words) {
      replacementString += divider + replacement;
      divider = " ";
    }
    return replacementString;
  }

  public BaseContainer<T> getWitness() {
    return _witness;
  }

  @Override
  public int getBeginPosition() {
    return startPosition;
  }

  @Override
  public int getEndPosition() {
    return endPosition;
  }

  public List<T> getWords() {
    final List<T> words = Lists.newArrayList();
    for (int k = getBeginPosition(); k <= getEndPosition(); k++) {
      final T word = getWitness().getWordOnPosition(k);
      words.add(word);
    }
    return words;
  }

  public T getFirstWord() {
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

  @Override
  public String getOriginal() {
    return toString();
  }

  @Override
  public String getWitnessId() {
    return getFirstWord().getWitnessId();
  }

}
