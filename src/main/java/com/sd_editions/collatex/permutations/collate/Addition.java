package com.sd_editions.collatex.permutations.collate;

import com.sd_editions.collatex.match.views.ModificationVisitor;

import eu.interedition.collatex.input.BaseContainerPart;
import eu.interedition.collatex.visualization.Modification;

public class Addition extends Modification {
  private final BaseContainerPart _part;
  private final int position;

  public Addition(int _position, BaseContainerPart part) {
    this.position = _position;
    this._part = part;
  }

  public int getPosition() {
    return position;
  }

  public String getAddedWords() {
    return _part.toString();
  }

  @Override
  public String toString() {
    return "addition: " + _part.toString() + " position: " + position;
  }

  @Override
  public void accept(ModificationVisitor modificationVisitor) {
    modificationVisitor.visitAddition(this);
  }

}
