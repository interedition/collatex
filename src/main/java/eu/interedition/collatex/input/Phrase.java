package eu.interedition.collatex.input;

import java.util.List;

import com.google.common.collect.Lists;
import com.sd_editions.collatex.match.Subsegment;

public class Phrase extends BaseElement {
  private final Segment witness;
  private final int startPosition;
  private final int endPosition;
  private final int size;
  private Subsegment _subSegment;

  // TODO: It is pretty obvious: too many parameters here!
  // Note: probably two constructors needed...
  // Note: one where the phrase resembles the words between two other words of the witness
  // Note: one where the start and end words of the phrase are given

  public Phrase(final Segment _witness, final int _size, final int _startPosition, final int _endPosition) {
    witness = _witness;
    this.size = _size;
    startPosition = _startPosition;
    endPosition = _endPosition;
  }

  // THIS constructor is pretty close, and actually used!
  public Phrase(final Segment _witness, final Word beginWord, final Word endWord, final Subsegment subsegment) {
    this.witness = _witness;
    this.startPosition = beginWord.position;
    this.endPosition = endWord.position;
    this._subSegment = subsegment;
    this.size = endWord.position - beginWord.position + 1;
  }

  public Phrase(final int _startPosition, final int _endPosition, final Subsegment subsegment) {
    this.startPosition = _startPosition;
    this.endPosition = _endPosition;
    this._subSegment = subsegment;
    this.size = endPosition - startPosition + 1;
    this.witness = null; // this is not wanted here!
  }

  //TODO: rename method!
  public boolean hasGap() {
    return size > 0;
  }

  @Override
  public String toString() {
    final List<String> words = Lists.newArrayList();
    for (int k = getStartPosition(); k <= getEndPosition(); k++) {
      final String word = witness.getElementOnWordPosition(k).toString();
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

  public Segment getWitness() {
    return witness;
  }

  public int getStartPosition() {
    return startPosition;
  }

  @Override
  public int getEndPosition() {
    return endPosition;
  }

  public List<Word> getWords() {
    final List<Word> words = Lists.newArrayList();
    for (int k = getStartPosition(); k <= getEndPosition(); k++) {
      final Word word = getWitness().getElementOnWordPosition(k);
      words.add(word);
    }
    return words;
  }

  public Word getFirstWord() {
    return getWords().get(0);
  }

  @Override
  public String getOriginal() {
    return toString();
  }

  @Override
  public int getBeginPosition() {
    return startPosition;
  }

  public Subsegment getSubsegment() {
    return _subSegment;
  }

  @Override
  public String getWitnessId() {
    return getFirstWord().getWitnessId();
  }

  @Override
  public int length() {
    return size;
  }
}
