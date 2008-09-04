package com.sd_editions.collatex.spike2;

public class Trans {

  private final Integer[] integers;
  private final Integer[] integers2;
  private final TransTuple[] tuples;

  public Trans(Integer[] integers, Integer[] integers2) {
    this.integers = integers;
    this.integers2 = integers2;
    this.tuples = calculateTuples();

  }

  private TransTuple[] calculateTuples() {
    TransTuple[] tuples = new TransTuple[integers.length];
    for (int i = 0; i < integers.length; i++) {
      tuples[i] = new TransTuple(integers[i], integers2[i], i + 1);
    }
    return tuples;
  }

  public TransTuple[] getTuples() {
    return tuples;
  }

}
