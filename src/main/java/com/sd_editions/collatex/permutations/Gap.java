package com.sd_editions.collatex.permutations;

import java.util.List;

import com.google.common.collect.Lists;

import eu.interedition.collatex.input.Witness;
import eu.interedition.collatex.input.Word;

public class Gap extends Phrase {
  private final int size;
  private final Word previous;
  private final Word next;

  public Gap(Witness _witness, int _size, int _beginPosition, int _endPosition, Word _previous, Word _next) {
    super(_witness, _beginPosition, _endPosition);
    this.size = _size;
    this.previous = _previous;
    this.next = _next;
  }

  public boolean hasGap() {
    return size > 0;
  }

  public Word getFirstWord() {
    return getWords().get(0);
  }

  // TODO: move up to Phrase?
  private List<Word> getWords() {
    List<Word> words = Lists.newArrayList();
    for (int k = getStartPosition(); k <= getEndPosition(); k++) {
      Word word = getWitness().getWordOnPosition(k);
      words.add(word);
    }
    return words;
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

}
