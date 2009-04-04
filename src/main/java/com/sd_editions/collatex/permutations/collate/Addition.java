package com.sd_editions.collatex.permutations.collate;

import com.sd_editions.collatex.match.views.ModificationVisitor;
import com.sd_editions.collatex.permutations.Modification;
import com.sd_editions.collatex.permutations.Phrase;

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

  @Override
  public void accept(ModificationVisitor modificationVisitor) {
    modificationVisitor.visitAddition(this);
  }

}
