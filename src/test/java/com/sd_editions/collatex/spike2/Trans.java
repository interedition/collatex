package com.sd_editions.collatex.spike2;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class Trans {

  private final Integer[] base;
  private final Integer[] witness;
  private final List<TransTuple> tuples;
  private final Set<Transposition> transpositions;

  public Trans(Integer[] integers, Integer[] integers2) {
    this.base = integers;
    this.witness = integers2;
    this.tuples = calculateTuples();
    this.transpositions = calculateTranspositions();
  }

  private Set<Transposition> calculateTranspositions() {
    List<TransTuple> _tuples = getTuples();
    List<Transposition> asT2 = Lists.newArrayList();
    for (TransTuple tuple : _tuples) {
      asT2.add(new Transposition(tuple));
    }
    return Sets.newHashSet(asT2);
  }

  private List<TransTuple> calculateTuples() {
    TransTuple[] tuples = new TransTuple[base.length];
    for (int i = 0; i < base.length; i++) {
      tuples[i] = new TransTuple(base[i], witness[i], i + 1);
    }
    return Arrays.asList(tuples);
  }

  public List<TransTuple> getTuples() {
    return tuples;
  }

  public Set<Transposition> getTranspositions() {
    return transpositions;
  }

}
