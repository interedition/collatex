package com.sd_editions.collatex.spike2;

public class TransTuple {
  private final Integer base;
  private final Integer witness;
  private final Integer position;

  public TransTuple(Integer base, Integer witness, Integer position) {
    this.base = base;
    this.witness = witness;
    this.position = position;

  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof TransTuple)) {
      return false;
    }
    TransTuple tuple = (TransTuple) obj;
    return tuple.base.equals(base) && tuple.witness.equals(witness) && tuple.position.equals(position);
  }

  @Override
  public String toString() {
    return "" + position + " -> (" + base + "," + witness + ")";
  }
}
