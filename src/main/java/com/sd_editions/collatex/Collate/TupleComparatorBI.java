package com.sd_editions.collatex.Collate;

import java.io.Serializable;
import java.util.Comparator;

public class TupleComparatorBI implements Comparator<Tuple>, Serializable {
  private static final long serialVersionUID = 2724931576152430838L;

  public int compare(Tuple tupA, Tuple tupB) {
    double valTup_A = transformTuple(tupA);
    double valTup_B = transformTuple(tupB);
    if (valTup_A < valTup_B) return -1;
    if (valTup_A > valTup_B) return 1;

    return 0;
  }

  @SuppressWarnings("boxing")
  private double transformTuple(Tuple r) {
    Double str1 = new Double(Integer.toString(r.getBaseIndex()));
    Double str2 = new Double(Integer.toString(r.getWitnessIndex()));

    return str1 + (str2 * 0.1);
  }
}
