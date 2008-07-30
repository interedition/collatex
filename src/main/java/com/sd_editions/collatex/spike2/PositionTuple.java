package com.sd_editions.collatex.spike2;

public class PositionTuple {
  private final int positionWitness1;
  private final int positionWitness2;

  public PositionTuple(int _positionWitness1, int _positionWitness2) {
    this.positionWitness1 = _positionWitness1;
    this.positionWitness2 = _positionWitness2;
  }

  public int getPositionWitness1() {
    return positionWitness1;
  }

  public int getPositionWitness2() {
    return positionWitness2;
  }
}
