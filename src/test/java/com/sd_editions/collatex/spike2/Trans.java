package com.sd_editions.collatex.spike2;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Trans {

  private final Integer[] integers;
  private final Integer[] integers2;
  private final TransTuple[] tuples;
  private final Set<TransTuple2> transpositions;

  public Trans(Integer[] integers, Integer[] integers2) {
    this.integers = integers;
    this.integers2 = integers2;
    this.tuples = calculateTuples();
    this.transpositions = calculateTranspositions();

  }

  private Set<TransTuple2> calculateTranspositions() {
    TransTuple2[] tuples2 = new TransTuple2[integers.length];
    for (int i = 0; i < integers.length; i++) {
      tuples2[i] = new TransTuple2(integers[i], integers2[i], i + 1);
    }
    List<TransTuple2> asList = Arrays.asList(tuples2);
    Set<TransTuple2> transpositions1 = new HashSet<TransTuple2>(asList);
    return transpositions1;
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

  public Set<TransTuple2> getTranspositions() {
    return transpositions;
  }

}
