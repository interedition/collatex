package com.sd_editions.collatex.permutations.collate;

import com.sd_editions.collatex.match.views.ModificationVisitor;

import eu.interedition.collatex.input.BaseContainerPart;
import eu.interedition.collatex.visualization.Modification;

public class Replacement extends Modification {
  private final BaseContainerPart _original;
  private final BaseContainerPart _replacement;

  public Replacement(final BaseContainerPart original, final BaseContainerPart replacement) {
    this._original = original;
    this._replacement = replacement;
  }

  @Override
  public String toString() {
    final String baseWords = _original.toString();
    final String replacementWords = _replacement.toString();
    return "replacement: " + baseWords + " / " + replacementWords + " position: " + _original.getBeginPosition();
  }

  public int getPosition() {
    return _original.getBeginPosition();
  }

  public String getOriginalWords() {
    return _original.toString();
  }

  public String getReplacementWords() {
    return _replacement.toString();
  }

  @Override
  public void accept(final ModificationVisitor modificationVisitor) {
    modificationVisitor.visitReplacement(this);
  }

}
