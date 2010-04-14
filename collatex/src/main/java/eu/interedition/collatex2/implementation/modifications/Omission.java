package eu.interedition.collatex2.implementation.modifications;

import eu.interedition.collatex2.interfaces.IColumns;
import eu.interedition.collatex2.interfaces.IOmission;

public class Omission implements IOmission {
  private final IColumns columns;

  public Omission(final IColumns gapA) {
    this.columns = gapA;
  }

  public IColumns getOmittedColumns() {
    return columns;
  }

  public int getPosition() {
    return columns.getFirstColumn().getPosition();
  }

  //TODO should not be getNormalized!
  @Override
  public String toString() {
    return "omission: " + columns.toString() + " position: " + getPosition();
  }

  //  @Override
  //  public void accept(final ModificationVisitor modificationVisitor) {
  //    modificationVisitor.visitOmission(this);
  //  }
}
