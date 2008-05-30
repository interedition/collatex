package com.sd_editions.collatex.functional;

import java.util.Arrays;
import java.util.Collections;

import com.sd_editions.collatex.Collate.Tuple;

public class TuplesArrayAndMaxHAndMaxV {
  private Tuple[] tuples;
  private int maxH, maxV;

  public TuplesArrayAndMaxHAndMaxV(int anz) {
    this.tuples = new Tuple[anz];
  }

  public int getMaxH() {
    return maxH;
  }

  public int getMaxV() {
    return maxV;
  }

  public Tuple[] getTuplesArray() {
    return tuples;
  }

  public void set(int i, Tuple next) {
    tuples[i] = next;
    if (next.baseIndex > maxH) {
      maxH = next.baseIndex;
    }
    if (next.witnessIndex > maxV) {
      maxV = next.witnessIndex;
    }
  }

  public void sort() {
    Collections.sort(Arrays.asList(tuples));
  }

}
