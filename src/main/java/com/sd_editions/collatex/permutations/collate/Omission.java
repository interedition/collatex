package com.sd_editions.collatex.permutations.collate;

import com.sd_editions.collatex.match.views.ModificationVisitor;

import eu.interedition.collatex.alignment.Phrase;
import eu.interedition.collatex.visualization.Modification;

public class Omission extends Modification {
  private final Phrase phrase;

  public Omission(Phrase _phrase) {
    this.phrase = _phrase;
  }

  public String getOmittedWords() {
    return phrase.toString();
  }

  public int getPosition() {
    return phrase.getStartPosition();
  }

  @Override
  public String toString() {
    return "omission: " + phrase.toString() + " position: " + phrase.getStartPosition();
  }

  @Override
  public void accept(ModificationVisitor modificationVisitor) {
    modificationVisitor.visitOmission(this);
  }
}
