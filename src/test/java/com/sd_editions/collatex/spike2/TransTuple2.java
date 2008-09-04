package com.sd_editions.collatex.spike2;

public class TransTuple2 extends TransTuple {

  public TransTuple2(Integer base, Integer witness, Integer position) {
    super(base, witness, position);
  }

  @Override
  public boolean equals(Object obj) {
    System.out.println("!!");
    if (!(obj instanceof TransTuple2)) {
      return false;
    }
    TransTuple2 tuple = (TransTuple2) obj;
    return tuple.base.equals(witness) && tuple.witness.equals(base);
  }

  @Override
  public int hashCode() {
    return base.hashCode() + witness.hashCode();
  }
}
