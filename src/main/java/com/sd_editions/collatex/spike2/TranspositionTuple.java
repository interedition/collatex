package com.sd_editions.collatex.spike2;

public class TranspositionTuple {

  private final MatchSequence matchSequence;

  public TranspositionTuple(MatchSequence matchSequence) {
    this.matchSequence = matchSequence;
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
    return matchSequence.toString();
  }

  Integer getRight() {
    return matchSequence.getWitnessPosition();
  }

  Integer getLeft() {
    return matchSequence.getBasePosition();
  }

}
