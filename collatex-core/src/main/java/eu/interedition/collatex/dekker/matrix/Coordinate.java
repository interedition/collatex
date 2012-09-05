package eu.interedition.collatex.dekker.matrix;

import com.google.common.base.Objects;

public class Coordinate implements Comparable<Coordinate> {
  int row;
  int column;

  public Coordinate(int row, int column) {
    this.column = column;
    this.row = row;
  }

  Coordinate(Coordinate other) {
    this(other.row, other.column);
  }

  public int getRow() {
    return row;
  }

  public int getColumn() {
    return column;
  }

  public boolean sameColumn(Coordinate c) {
    return c.column == column;
  }

  public boolean sameRow(Coordinate c) {
    return c.row == row;
  }

  public boolean bordersOn(Coordinate c) {
    return (Math.abs(this.row - c.getRow()) == 1) && (Math.abs(this.column - c.getColumn()) == 1);
  }

  @Override
  public boolean equals(Object o) {
    if (o != null & o instanceof Coordinate) {
      final Coordinate c = (Coordinate) o;
      return (this.row == c.getRow() && this.column == c.getColumn());
    }
    return super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(row, column);
  }

  @Override
  public int compareTo(Coordinate o) {
    final int result = column - o.column;
    return (result == 0 ? row - o.row : result);
  }

  @Override
  public String toString() {
    return "(" + row + "," + column + ")";
  }
}
