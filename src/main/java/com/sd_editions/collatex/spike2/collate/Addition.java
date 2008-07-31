package com.sd_editions.collatex.spike2.collate;

import com.sd_editions.collatex.spike2.Modification;
import com.sd_editions.collatex.spike2.Phrase;

public class Addition extends Modification {
  private final Phrase phrase;
  private final int position;

  public Addition(int _position, Phrase _phrase) {
    this.position = _position;
    this.phrase = _phrase;
  }

  public int getPosition() {
    return position;
  }

  public String getAddedWords() {
    return phrase.toString();
  }

  @Override
  public String toString() {
    return "addition: " + phrase.toString() + " position: " + position;
  }

}
