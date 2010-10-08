package eu.interedition.collatex.experimental.ngrams.alignment;


import eu.interedition.collatex.experimental.ngrams.NGram;

// TODO rename phrase to part!
public class Omission extends Modification {
  private final NGram phrase;

  public Omission(final NGram _phrase) {
    this.phrase = _phrase;
  }

  public NGram getOmittedWords() {
    return phrase;
  }

  public int getPosition() {
    return phrase.getFirstToken().getPosition();
  }

  //TODO should not be getNormalized!
  @Override
  public String toString() {
    return "omission: " + phrase.getNormalized() + " position: " + phrase.getFirstToken().getPosition();
  }

  @Override
  public void accept(final ModificationVisitor modificationVisitor) {
    modificationVisitor.visitOmission(this);
  }
}
