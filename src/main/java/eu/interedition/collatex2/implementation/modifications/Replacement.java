package eu.interedition.collatex2.implementation.modifications;

import eu.interedition.collatex2.interfaces.IModification;
import eu.interedition.collatex2.interfaces.IPhrase;

public class Replacement implements IModification {
  private final IPhrase _original;
  private final IPhrase _replacement;

  public Replacement(final IPhrase original, final IPhrase replacement) {
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

  public IPhrase getOriginalWords() {
    return _original;
  }

  public IPhrase getReplacementWords() {
    return _replacement;
  }

  //  @Override
  //  public void accept(final ModificationVisitor modificationVisitor) {
  //    modificationVisitor.visitReplacement(this);
  //  }

}
