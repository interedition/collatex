package com.sd_editions.collatex.spike2;

import java.util.List;

import com.google.common.collect.Lists;

public class Phrase {
  private final Witness witness;
  private final int startPosition;
  private final int endPosition;

  public Phrase(Witness _witness, int _startPosition, int _endPosition) {
    witness = _witness;
    startPosition = _startPosition;
    endPosition = _endPosition;
  }

  @Override
  public String toString() {
    List<String> words = Lists.newArrayList();
    for (int k = getStartPosition(); k <= getEndPosition(); k++) {
      String word = witness.getWordOnPosition(k);
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

  public int getStartPosition() {
    return startPosition;
  }

  public int getEndPosition() {
    return endPosition;
  }

}
