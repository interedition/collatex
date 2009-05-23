package com.sd_editions.collatex.permutations.collate;

import com.sd_editions.collatex.permutations.Modification;

import eu.interedition.collatex.collation.sequences.MatchSequence;

public class Transposition extends Modification {
  private final MatchSequence base;
  private final MatchSequence witness;

  public Transposition(MatchSequence _base, MatchSequence _witness) {
    this.base = _base;
    this.witness = _witness;
  }

  @Override
  public String toString() {
    return "transposition: " + getLeft() + " switches position with " + getRight();
  }

  public String getLeft() {
    return base.baseToString();
  }

  public String getRight() {
    return witness.baseToString();
  }
}
