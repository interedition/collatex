package com.sd_editions.collatex.permutations;

import eu.interedition.collatex.alignment.MatchSequence;
import eu.interedition.collatex.input.BaseElement;

public class TranspositionTuple<T extends BaseElement> {

  private final Tuple2<MatchSequence<T>> tuple;

  public TranspositionTuple(final Tuple2<MatchSequence<T>> _tuple) {
    this.tuple = _tuple;
  }

  @Override
  public boolean equals(final Object obj) {
    if (!(obj instanceof TranspositionTuple)) {
      return false;
    }
    final TranspositionTuple tuple2 = (TranspositionTuple) obj;
    final boolean result = tuple2.getLeftWordCode().equals(getLeftWordCode()) && tuple2.getRightWordCode().equals(getRightWordCode());
    //    System.out.println("comparing: " + this.toString() + " && " + tuple2.toString() + " result: " + result);
    return result;
  }

  @Override
  public int hashCode() {
    return getLeftWordCode().hashCode() + getRightWordCode().hashCode();
  }

  @Override
  public String toString() {
    return tuple.toString();
  }

  MatchSequence getLeftSequence() {
    return tuple.left;
  }

  MatchSequence getRightSequence() {
    return tuple.right;
  }

  Integer getRightWordCode() {
    return getRightSequence().code;
  }

  Integer getLeftWordCode() {
    return getLeftSequence().code;
  }

}
