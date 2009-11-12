package com.sd_editions.collatex.permutations;

import eu.interedition.collatex.alignment.Match;
import eu.interedition.collatex.input.BaseElement;

public class Tuple2<T> {
  final T left;
  final T right;
  final Match<BaseElement> _nextMatch;

  public Tuple2(final T _left, final T _right, final Match nextMatch) {
    this.left = _left;
    this.right = _right;
    this._nextMatch = nextMatch;
  }

  @Override
  public boolean equals(final Object obj) {
    if (!(obj instanceof Tuple2)) return false;
    final Tuple2<?> other_tuple = (Tuple2<?>) obj;
    return other_tuple.left.equals(left) && other_tuple.right.equals(right);
  }

  @Override
  public int hashCode() {
    return left.hashCode() + right.hashCode();
  }

  @Override
  public String toString() {
    return "{" + left.toString() + "; " + right.toString() + "}";
  }
}
