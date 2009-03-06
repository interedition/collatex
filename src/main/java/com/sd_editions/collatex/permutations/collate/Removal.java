package com.sd_editions.collatex.permutations.collate;

import com.sd_editions.collatex.permutations.Modification;
import com.sd_editions.collatex.permutations.Phrase;

public class Removal extends Modification {
  private final Phrase phrase;

  public Removal(Phrase _phrase) {
    this.phrase = _phrase;
  }

  public String getRemovedWords() {
    return phrase.toString();
  }

  public int getPosition() {
    return phrase.getStartPosition();
  }

  @Override
  public String toString() {
    return "omission: " + phrase.toString() + " position: " + phrase.getStartPosition();
  }
}
