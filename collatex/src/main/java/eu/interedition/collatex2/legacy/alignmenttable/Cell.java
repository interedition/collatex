package eu.interedition.collatex2.legacy.alignmenttable;

import eu.interedition.collatex2.interfaces.ICell;
import eu.interedition.collatex2.interfaces.IInternalColumn;
import eu.interedition.collatex2.interfaces.INormalizedToken;

public class Cell implements ICell {
  private final IInternalColumn column;
  private final String sigil;

  public Cell(IInternalColumn column, String sigil) {
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
