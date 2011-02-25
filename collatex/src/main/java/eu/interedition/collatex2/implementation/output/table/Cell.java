package eu.interedition.collatex2.implementation.output.table;

import eu.interedition.collatex2.interfaces.ICell;
import eu.interedition.collatex2.interfaces.IColumn;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IVariantGraphVertex;
import eu.interedition.collatex2.interfaces.IWitness;

public class Cell implements ICell {
  private final IColumn column;
  private final IWitness witness;

  public Cell(IColumn column, IWitness witness) {
    this.column = column;
    this.witness = witness;
  }

  @Override
  public IColumn getColumn() {
    return column;
  }

  @Override
  public String getColor() {
    IVariantGraphVertex vertexForWitness = ((VariantGraphBasedColumn) column).findVertexForWitness(witness);
    return vertexForWitness == null ? "black" : color(vertexForWitness.getVertexKey().hashCode());
  }

  private String color(int hashCode) {
    return "#" + (Integer.toHexString(hashCode) + "000000").substring(0, 6);
  }

  @Override
  public INormalizedToken getToken() {
    return column.getToken(witness);
  }

  @Override
  public boolean isEmpty() {
    return !column.containsWitness(witness);
  }
}
