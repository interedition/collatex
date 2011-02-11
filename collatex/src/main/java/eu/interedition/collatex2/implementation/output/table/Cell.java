package eu.interedition.collatex2.implementation.output.table;

import eu.interedition.collatex2.interfaces.ICell;
import eu.interedition.collatex2.interfaces.IInternalColumn;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IVariantGraphVertex;

public class Cell implements ICell {
  private final IInternalColumn column;

  private final String sigil;

  public Cell(IInternalColumn column, String sigil) {
    this.column = column;
    this.sigil = sigil;
  }

  @Override
  public IInternalColumn getColumn() {
    return column;
  }

  @Override
  public int getPosition() {
    return column.getPosition();
  }

  @Override
  public String getColor(String sigil) {
    IVariantGraphVertex findVertexForWitness = ((VariantGraphBasedColumn) column).findVertexForWitness(sigil);
    return findVertexForWitness == null ? "black" : color(findVertexForWitness.getVertexKey().hashCode());
  }

  private String color(int hashCode) {
    return "#" + (Integer.toHexString(hashCode) + "000000").substring(0, 6);
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
