package com.sd_editions.collatex.match;

public class ColorMatrix {
  static final int END_OF_WITNESS = -1;
  static final int NO_COLOR_ASSIGNED = 0;

  private final int width;
  private final int height;

  private final int[][] matrix;

  public ColorMatrix(int height1, int width1) {
    this.height = height1;
    this.width = width1;
    matrix = new int[height][width];
    for (int row = 0; row < this.height; row++) {
      for (int col = 0; col < this.width; col++) {
        setCell(row, col, NO_COLOR_ASSIGNED);
      }
    }
  }

  public ColorMatrix(ColorMatrix seedmatrix) {
    this.height = seedmatrix.height;
    this.width = seedmatrix.width;
    matrix = new int[height][width];
    for (int row = 0; row < this.height; row++) {
      for (int col = 0; col < this.width; col++) {
        setCell(row, col, seedmatrix.getCell(row, col));
      }
    }
  }

  public ColorMatrix(int[][] seedmatrix) {
    this.height = seedmatrix.length;
    this.width = seedmatrix[0].length;
    matrix = new int[height][width];
    for (int row = 0; row < this.height; row++) {
      for (int col = 0; col < this.width; col++) {
        setCell(row, col, seedmatrix[row][col]);
      }
    }
  }

  public int getCell(int row, int col) {
    return matrix[row][col];
  }

  public int setCell(int row, int col, int val) {
    return matrix[row][col] = val;
  }

  @Override
  public int hashCode() {
    int hashCode = 0;
    for (int row = 0; row < this.height; row++) {
      for (int col = 0; col < this.width; col++) {
        hashCode += this.getCell(row, col);
      }
      hashCode *= row + 1;
    }
    return hashCode;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof ColorMatrix)) return false;
    ColorMatrix otherColorMatrix = (ColorMatrix) obj;
    if (this.height != otherColorMatrix.height) return false;
    if (this.width != otherColorMatrix.width) return false;

    boolean equals = true;
    for (int row = 0; row < this.height; row++) {
      for (int col = 0; col < this.width; col++) {
        equals &= this.getCell(row, col) == otherColorMatrix.getCell(row, col);
      }
    }
    return equals;
  }

  @Override
  public String toString() {
    String string = "";
    for (int[] row : matrix) {
      string += "| ";
      for (int cell : row) {
        string += ((cell < 10 && cell >= 0 ? " " : "") + cell + " ");
      }
      string += "|\n";
    }
    return string;
  }

  public int getWidth() {
    return width;
  }

  public int getHeight() {
    return height;
  }

}
