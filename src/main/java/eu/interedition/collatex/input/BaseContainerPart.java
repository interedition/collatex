package eu.interedition.collatex.input;

import java.util.List;

import com.google.common.collect.Lists;


// Note: this class started life as a duplicate of
// Phase; it will get a life of its own
// for instance...
// it might not need to extend BaseElement?
// anyway it should take BaseElements in
public class BaseContainerPart extends BaseElement {
  private final BaseContainer _witness;
  private final int startPosition;
  private final int endPosition;
  private final int size;
  private final Word previous;
  private final Word next;

  // TODO: It is pretty obvious: too many parameters here!
  // Note: probably two constructors needed...
  // Note: one where the phrase resembles the words between two other words of the witness
  // Note: one where the start and end words of the phrase are given

  public BaseContainerPart(final BaseContainer witness, final int _size, final int _startPosition, final int _endPosition, final Word _previous, final Word _next) {
    this._witness = witness;
    this.size = _size;
    this.next = _next;
    this.previous = _previous;
    startPosition = _startPosition;
    endPosition = _endPosition;
  }

  public BaseContainerPart(final BaseContainer witness, final Word beginWord, final Word endWord) {
    this._witness = witness;
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
    final List<String> words = Lists.newArrayList();
    for (int k = getStartPosition(); k <= getEndPosition(); k++) {
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

  public BaseContainer getWitness() {
    return _witness;
  }

  public int getStartPosition() {
    return startPosition;
  }

  public int getEndPosition() {
    return endPosition;
  }

  public List<Word> getWords() {
    final List<Word> words = Lists.newArrayList();
    for (int k = getStartPosition(); k <= getEndPosition(); k++) {
      final Word word = getWitness().getWordOnPosition(k);
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

  @Override
  public String getOriginal() {
    return toString();
  }

  @Override
  public int getPosition() {
    return startPosition;
  }
}
