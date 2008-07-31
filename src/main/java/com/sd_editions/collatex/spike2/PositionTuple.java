package com.sd_editions.collatex.spike2;

public class PositionTuple {
  public final int baseIndex;
  public final int witnessIndex;

  public PositionTuple(int _positionWitness1, int _positionWitness2) {
    this.baseIndex = _positionWitness1;
    this.witnessIndex = _positionWitness2;
  }

}
