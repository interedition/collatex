package com.sd_editions.collatex.Collate;

public class Tuple {
  int baseIndex;
  int witnessIndex;

  public Tuple(int baseIndex, int witnessIndex) {
    this.baseIndex = baseIndex;
    this.witnessIndex = witnessIndex;
  }

  @Override
  public String toString() {
    return "[" + baseIndex + "," + witnessIndex + "]";
  }
}
