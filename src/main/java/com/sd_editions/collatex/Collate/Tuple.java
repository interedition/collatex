package com.sd_editions.collatex.Collate;

public class Tuple implements Comparable {
  int baseIndex;
  int witnessIndex;

  public Tuple(int baseIndex, int witnessIndex) {
    this.baseIndex = baseIndex;
    this.witnessIndex = witnessIndex;
  }

  public int getBaseIndex() {
    return baseIndex;
  }

  public int getWitnessIndex() {
    return witnessIndex;
  }

  public int compareTo(Object obj) {
    Tuple tmp = (Tuple) obj;
    double newer = tmp.baseIndex + (double) tmp.witnessIndex / 10;
    double me = this.baseIndex + (double) this.witnessIndex / 10;
    if (me < newer) {
      return -1;
    } else if (me > newer) {
      return 1;
    }
    return 0;
  }

  @Override
  public String toString() {
    return "[" + baseIndex + "," + witnessIndex + "]";
  }
}
