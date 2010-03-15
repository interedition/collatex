package eu.interedition.collatex2.implementation.modifications;

import eu.interedition.collatex2.interfaces.IModification;
import eu.interedition.collatex2.interfaces.INGram;

public class Addition implements IModification {
  private final INGram addition;
  private final INGram nextMatchA;

  public Addition(final INGram nextMatchA, final INGram addition) {
    this.nextMatchA = nextMatchA;
    this.addition = addition;
  }

  public int getPosition() {
    if (nextMatchA == null || nextMatchA.isEmpty()) {
      throw new RuntimeException("There is no next match!");
    }
    return nextMatchA.getFirstToken().getPosition();
  }

  public INGram getAddedWords() {
    return addition;
  }

  @Override
  public String toString() {
    // TODO: should not be get Normalized?
    String result = "addition: " + addition.getNormalized();
    // TODO: I would like to have only 
    if (nextMatchA == null || nextMatchA.isEmpty()) {
      result += " position: at the end";
    } else {
      result += " position: " + getPosition();
    }
    return result;
  }

  //  @Override
  //  public void accept(final ModificationVisitor modificationVisitor) {
  //    modificationVisitor.visitAddition(this);
  //  }

}
