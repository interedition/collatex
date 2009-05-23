package com.sd_editions.collatex.match.views;

import eu.interedition.collatex.input.Word;

public class TextElement extends Element {

  private final Word baseWord;

  public TextElement(Word baseWord) {
    this.baseWord = baseWord;
  }

  @Override
  public String toXML() {
    return baseWord.toString();
  }

}
