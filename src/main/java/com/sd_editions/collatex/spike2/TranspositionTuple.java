package com.sd_editions.collatex.spike2;

public class TranspositionTuple {

  private final Tuple2<MatchSequence> tuple;

  public TranspositionTuple(Tuple2<MatchSequence> tuple) {
    this.tuple = tuple;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof TranspositionTuple)) {
      return false;
    }
    TranspositionTuple tuple2 = (TranspositionTuple) obj;
    return tuple2.getLeft().equals(getRight()) && tuple2.getRight().equals(getLeft());
  }

  @Override
  public int hashCode() {
    return getLeft().hashCode() + getRight().hashCode();
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

  Integer getRight() {
    return tuple.right.getFirstMatch().wordCode;
  }

  Integer getLeft() {
    return tuple.left.getFirstMatch().wordCode;
  }

}
