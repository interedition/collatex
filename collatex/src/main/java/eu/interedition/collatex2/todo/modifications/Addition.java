package eu.interedition.collatex2.todo.modifications;

import eu.interedition.collatex2.interfaces.IInternalColumn;
import eu.interedition.collatex2.interfaces.IPhrase;
import eu.interedition.collatex2.interfaces.nonpublic.modifications.IAddition;
import eu.interedition.collatex2.interfaces.nonpublic.modifications.IGap;

public class Addition implements IAddition {
  private final IPhrase addition;
  private final IInternalColumn nextColumn;

  private Addition(final IInternalColumn nextColumn, final IPhrase addition) {
    this.nextColumn = nextColumn;
    this.addition = addition;
  }

  public int getPosition() {
    return -1;
  }

  @Override
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
  public boolean isAtTheBeginning() {
    throw new RuntimeException("NOT implemented!");
  }

  @Override
  public boolean isAtTheEnd() {
    return nextColumn == null;
  }

  @Override
  public IInternalColumn getNextColumn() {
    if (isAtTheEnd()) {
      throw new RuntimeException("There is no next match!");
    }
    return nextColumn;
  }

  public static IAddition create(IGap gap) {
    return new Addition(gap.getNextColumn(), gap.getPhrase());
  }


  //  @Override
  //  public void accept(final ModificationVisitor modificationVisitor) {
  //    modificationVisitor.visitAddition(this);
  //  }

}
