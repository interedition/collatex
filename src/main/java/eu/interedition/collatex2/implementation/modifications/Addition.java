package eu.interedition.collatex2.implementation.modifications;

import eu.interedition.collatex2.interfaces.IAddition;
import eu.interedition.collatex2.interfaces.IColumn;
import eu.interedition.collatex2.interfaces.IPhrase;

public class Addition implements IAddition {
  private final IPhrase addition;
  private final IColumn nextColumn;

  public Addition(final IColumn nextColumn, final IPhrase addition) {
    this.nextColumn = nextColumn;
    this.addition = addition;
  }

  public int getPosition() {
    return getNextColumn().getPosition();
  }

  public IPhrase getAddedPhrase() {
    return addition;
  }

  @Override
  public String toString() {
    // TODO should not be get Normalized?
    String result = "addition: " + addition.getNormalized();
    // TODO I would like to have only 
    if (isAtTheEnd()) {
      result += " position: at the end";
    } else {
      result += " position: " + getPosition();
    }
    return result;
  }

  @Override
  public boolean isAtTheEnd() {
    return nextColumn == null;
  }

  @Override
  public IColumn getNextColumn() {
    if (isAtTheEnd()) {
      throw new RuntimeException("There is no next match!");
    }
    return nextColumn;
  }

  //  @Override
  //  public void accept(final ModificationVisitor modificationVisitor) {
  //    modificationVisitor.visitAddition(this);
  //  }

}
