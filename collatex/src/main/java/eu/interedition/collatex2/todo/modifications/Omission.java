package eu.interedition.collatex2.todo.modifications;

import eu.interedition.collatex2.interfaces.IColumns;
import eu.interedition.collatex2.interfaces.IGap;
import eu.interedition.collatex2.interfaces.IOmission;

public class Omission implements IOmission {
  private final IColumns columns;

  private Omission(final IColumns gapA) {
    this.columns = gapA;
  }

  @Override
  public IColumns getOmittedColumns() {
    return columns;
  }

  @Override
  public int getPosition() {
    return columns.getFirstColumn().getPosition();
  }

  //TODO should not be getNormalized!
  @Override
  public String toString() {
    return "omission: " + columns.toString() + " position: " + getPosition();
  }

  public static IOmission create(IGap gap) {
    return new Omission(gap.getColumns());
  }

  //  @Override
  //  public void accept(final ModificationVisitor modificationVisitor) {
  //    modificationVisitor.visitOmission(this);
  //  }
}
