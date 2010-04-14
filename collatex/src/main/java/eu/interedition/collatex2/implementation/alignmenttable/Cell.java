package eu.interedition.collatex2.implementation.alignmenttable;

import eu.interedition.collatex2.interfaces.ICell;
import eu.interedition.collatex2.interfaces.IColumn;
import eu.interedition.collatex2.interfaces.INormalizedToken;

public class Cell implements ICell {
  private final IColumn column;
  private final String sigil;

  public Cell(IColumn column, String sigil) {
    this.column = column;
    this.sigil = sigil;
  }

  @Override
  public int getPosition() {
    return column.getPosition();
  }

  @Override
  public INormalizedToken getToken() {
    return column.getToken(sigil);
  }

  @Override
  public boolean isEmpty() {
    return !column.containsWitness(sigil);
  }
}
