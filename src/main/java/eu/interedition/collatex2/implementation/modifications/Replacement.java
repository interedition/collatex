package eu.interedition.collatex2.implementation.modifications;

import eu.interedition.collatex2.interfaces.IColumns;
import eu.interedition.collatex2.interfaces.IPhrase;
import eu.interedition.collatex2.interfaces.IReplacement;

public class Replacement implements IReplacement {
  private final IColumns _original;
  private final IPhrase _replacement;

  public Replacement(final IColumns gapA, final IPhrase replacement) {
    this._original = gapA;
    this._replacement = replacement;
  }

  @Override
  public String toString() {
    final String baseWords = _original.toString();
    // TODO Not getNormalized!
    final String replacementWords = _replacement.getNormalized();
    return "replacement: " + baseWords + " / " + replacementWords + " position: " + _original.getFirstColumn().getPosition();
  }

  public int getPosition() {
    return _original.getFirstColumn().getPosition();
  }

  public IColumns getOriginalColumns() {
    return _original;
  }

  public IPhrase getReplacementPhrase() {
    return _replacement;
  }

  //  @Override
  //  public void accept(final ModificationVisitor modificationVisitor) {
  //    modificationVisitor.visitReplacement(this);
  //  }

}
