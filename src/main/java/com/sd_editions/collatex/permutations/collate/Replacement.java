package com.sd_editions.collatex.permutations.collate;

import com.sd_editions.collatex.match.views.ModificationVisitor;

import eu.interedition.collatex.input.BaseContainerPart;
import eu.interedition.collatex.visualization.Modification;

public class Replacement extends Modification {
  private final BaseContainerPart _original;
  private final BaseContainerPart _replacement;

  public Replacement(BaseContainerPart original, BaseContainerPart replacement) {
    this._original = original;
    this._replacement = replacement;
  }

  @Override
  public String toString() {
    String baseWords = _original.toString();
    String replacementWords = _replacement.toString();
    return "replacement: " + baseWords + " / " + replacementWords + " position: " + _original.getStartPosition();
  }

  public int getPosition() {
    return _original.getStartPosition();
  }

  public String getOriginalWords() {
    return _original.toString();
  }

  public String getReplacementWords() {
    return _replacement.toString();
  }

  @Override
  public void accept(ModificationVisitor modificationVisitor) {
    modificationVisitor.visitReplacement(this);
  }

}
