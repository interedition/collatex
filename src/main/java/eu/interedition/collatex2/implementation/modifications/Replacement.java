package eu.interedition.collatex2.implementation.modifications;

import eu.interedition.collatex2.interfaces.IModification;
import eu.interedition.collatex2.interfaces.INGram;

public class Replacement implements IModification {
  private final INGram _original;
  private final INGram _replacement;

  public Replacement(final INGram original, final INGram replacement) {
    this._original = original;
    this._replacement = replacement;
  }

  @Override
  public String toString() {
    // TODO: Not getNormalized!
    final String baseWords = _original.getNormalized();
    // TODO: Not getNormalized!
    final String replacementWords = _replacement.getNormalized();
    return "replacement: " + baseWords + " / " + replacementWords + " position: " + _original.getFirstToken().getPosition();
  }

  public int getPosition() {
    return _original.getFirstToken().getPosition();
  }

  public INGram getOriginalWords() {
    return _original;
  }

  public INGram getReplacementWords() {
    return _replacement;
  }

  //  @Override
  //  public void accept(final ModificationVisitor modificationVisitor) {
  //    modificationVisitor.visitReplacement(this);
  //  }

}
