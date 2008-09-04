package com.sd_editions.collatex.spike2;

public class Transposition {

  private final TransTuple tuple;

  public Transposition(TransTuple tuple) {
    this.tuple = tuple;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Transposition)) {
      return false;
    }
    Transposition tuple2 = (Transposition) obj;
    return tuple2.tuple.base.equals(tuple.witness) && tuple2.tuple.witness.equals(tuple.base);
  }

  @Override
  public int hashCode() {
    return tuple.base.hashCode() + tuple.witness.hashCode();
  }
}
