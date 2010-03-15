package eu.interedition.collatex2.implementation.modifications;

import eu.interedition.collatex2.interfaces.IModification;
import eu.interedition.collatex2.interfaces.INGram;

public class Omission implements IModification {
  private final INGram phrase;

  public Omission(final INGram _phrase) {
    this.phrase = _phrase;
  }

  public INGram getOmittedWords() {
    return phrase;
  }

  public int getPosition() {
    return phrase.getFirstToken().getPosition();
  }

  //TODO: should not be getNormalized!
  @Override
  public String toString() {
    return "omission: " + phrase.getNormalized() + " position: " + phrase.getFirstToken().getPosition();
  }

  //  @Override
  //  public void accept(final ModificationVisitor modificationVisitor) {
  //    modificationVisitor.visitOmission(this);
  //  }
}
