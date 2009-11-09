package com.sd_editions.collatex.permutations.collate;

import com.sd_editions.collatex.match.views.ModificationVisitor;

import eu.interedition.collatex.input.BaseContainerPart;
import eu.interedition.collatex.visualization.Modification;

// TODO: rename phrase to part!
public class Omission extends Modification {
  private final BaseContainerPart phrase;

  public Omission(final BaseContainerPart _phrase) {
    this.phrase = _phrase;
  }

  public String getOmittedWords() {
    return phrase.toString();
  }

  public int getPosition() {
    return phrase.getBeginPosition();
  }

  @Override
  public String toString() {
    return "omission: " + phrase.toString() + " position: " + phrase.getBeginPosition();
  }

  @Override
  public void accept(final ModificationVisitor modificationVisitor) {
    modificationVisitor.visitOmission(this);
  }
}
