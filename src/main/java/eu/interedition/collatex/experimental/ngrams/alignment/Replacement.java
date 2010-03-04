package eu.interedition.collatex.experimental.ngrams.alignment;


import eu.interedition.collatex.experimental.ngrams.NGram;

public class Replacement extends Modification {
  private final NGram _original;
  private final NGram _replacement;

  public Replacement(final NGram original, final NGram replacement) {
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

  public NGram getOriginalWords() {
    return _original;
  }

  public NGram getReplacementWords() {
    return _replacement;
  }

  @Override
  public void accept(final ModificationVisitor modificationVisitor) {
    modificationVisitor.visitReplacement(this);
  }

}
