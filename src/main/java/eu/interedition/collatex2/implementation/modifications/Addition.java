package eu.interedition.collatex2.implementation.modifications;

import eu.interedition.collatex2.interfaces.IAddition;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IPhrase;

public class Addition implements IAddition {
  private final IPhrase addition;
  private final INormalizedToken nextMatchTokenA;

  public Addition(final INormalizedToken nextMatchTokenA, final IPhrase addition) {
    this.nextMatchTokenA = nextMatchTokenA;
    this.addition = addition;
  }

  public int getPosition() {
    return getNextMatchToken().getPosition();
  }

  public IPhrase getAddedWords() {
    return addition;
  }

  @Override
  public String toString() {
    // TODO: should not be get Normalized?
    String result = "addition: " + addition.getNormalized();
    // TODO: I would like to have only 
    if (isAtTheEnd()) {
      result += " position: at the end";
    } else {
      result += " position: " + getPosition();
    }
    return result;
  }

  @Override
  public boolean isAtTheEnd() {
    return nextMatchTokenA == null;
  }

  @Override
  public INormalizedToken getNextMatchToken() {
    if (isAtTheEnd()) {
      throw new RuntimeException("There is no next match!");
    }
    return nextMatchTokenA;
  }

  //  @Override
  //  public void accept(final ModificationVisitor modificationVisitor) {
  //    modificationVisitor.visitAddition(this);
  //  }

}
