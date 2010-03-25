package com.sd_editions.collatex.match.views;

import eu.interedition.collatex.experimental.ngrams.NGram;

public class AppElement extends Element {
  // TODO: rename!
  private final NGram addedWords;
  private final NGram reading;

  public AppElement(final NGram addedWords2) {
    this.addedWords = addedWords2;
    this.reading = null;
  }

  public AppElement(final NGram lemma, final NGram reading2) {
    this.addedWords = lemma;
    this.reading = reading2;
  }

  // TODO: use StringBuilder!
  @Override
  public String toXML() {
    String result = "<app>";
    if (reading == null) {
      //TODO: should not be getNormalized!
      result += addedWords.getNormalized();
    } else {
      //TODO: should not be getNormalized!
      result += "<lemma>" + addedWords.getNormalized() + "</lemma>";
      //TODO: should not be getNormalized!
      result += "<reading>" + reading.getNormalized() + "</reading>";
    }
    result += "</app>";
    return result;
  }
}
