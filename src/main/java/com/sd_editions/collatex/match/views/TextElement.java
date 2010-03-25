package com.sd_editions.collatex.match.views;

import eu.interedition.collatex.experimental.ngrams.NGram;

public class TextElement extends Element {

  private final NGram baseWord;

  public TextElement(final NGram match) {
    this.baseWord = match;
  }

  @Override
  public String toXML() {
    // TODO: should not be normalized!
    return baseWord.getNormalized();
  }

}
