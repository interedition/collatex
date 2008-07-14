package com.sd_editions.collatex.Collate;

import java.util.Comparator;

public class TupleComparatorBI implements Comparator<Tuple> {
  public int compare(Tuple tupA, Tuple tupB) {
    if (tupA.baseIndex < tupB.baseIndex) {
      return -1;
    } else if (tupA.baseIndex > tupB.baseIndex) {
      return 1;
    }
    return 0;
  }
}
