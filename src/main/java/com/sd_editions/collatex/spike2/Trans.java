package com.sd_editions.collatex.spike2;

import java.util.List;
import java.util.Set;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class Trans {

  private final List<Integer> base;
  private final List<Integer> witness;
  private final List<TransTuple> tuples;
  private final Set<Transposition> transpositions;

  public Trans(List<Integer> integers, List<Integer> integers2) {
    this.base = integers;
    this.witness = integers2;
    this.tuples = calculateTuples();
    this.transpositions = calculateTranspositions();
  }

  private Set<Transposition> calculateTranspositions() {
    List<TransTuple> _tuples = getTuples();
    List<TransTuple> _filteredTuples = Lists.newArrayList(Iterables.filter(_tuples, new Predicate<TransTuple>() {
      public boolean apply(TransTuple tuple) {
        return tuple.base != tuple.witness;
      }
    }));
    List<Transposition> asT2 = Lists.newArrayList();
    for (TransTuple tuple : _filteredTuples) {
      asT2.add(new Transposition(tuple));
    }
    return Sets.newHashSet(asT2);
  }

  @SuppressWarnings("boxing")
  private List<TransTuple> calculateTuples() {
    List<TransTuple> _tuples = Lists.newArrayList();
    for (int i = 0; i < base.size(); i++) {
      _tuples.add(new TransTuple(base.get(i), witness.get(i), i + 1));
    }
    return _tuples;
  }

  public List<TransTuple> getTuples() {
    return tuples;
  }

  public Set<Transposition> getTranspositions() {
    return transpositions;
  }

}
