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
    boolean result = tuple2.getLeftWordCode().equals(getRightWordCode()) && tuple2.getRightWordCode().equals(getLeftWordCode());
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
    return getRightSequence().getFirstMatch().wordCode;
  }

  Integer getLeftWordCode() {
    return getLeftSequence().getFirstMatch().wordCode;
  }

}
