package eu.interedition.collatex.MatrixLinker;

import com.google.common.base.Objects;

public class MatchMatrixCell {
  int row;
  int col;

  MatchMatrixCell(int col, int row) {
    this.col = col;
    this.row = row;
  }

  public int getRow() {
    return row;
  }

  public int getCol() {
    return col;
  }

  public boolean sameCol(MatchMatrixCell c) {
    return c.col == col;
  }

  public boolean sameRow(MatchMatrixCell c) {
    return c.row == row;
  }

  public boolean borders(MatchMatrixCell c) {
    boolean res = Math.abs(this.row - c.getRow()) == 1;
    res &= Math.abs(this.col - c.getCol()) == 1;
    return res;
  }

  public MatchMatrixCell copy() {
    return new MatchMatrixCell(col, row);
  }

  public boolean partOf(Archipelago arch) {
    boolean result = false;
    for (MatchMatrixIsland isl : arch.iterator()) {
      result |= isl.partOf(this);
    }
    return result;
  }

  @Override
  public boolean equals(Object o) {
    if (o != null & o instanceof MatchMatrixCell) {
      final MatchMatrixCell c = (MatchMatrixCell) o;
      return (this.row == c.getRow() && this.col == c.getCol());
    }
    return super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(row, col);
  }

  @Override
  public String toString() {
    return "(" + col + "," + row + ")";
  }
}
