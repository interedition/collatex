package com.sd_editions.collatex.permutations;

import java.util.List;

import com.google.common.collect.Lists;

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

  public Word getPreviousWord() {
    return previous;
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

}
