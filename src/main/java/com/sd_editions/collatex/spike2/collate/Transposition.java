package com.sd_editions.collatex.spike2.collate;

import com.sd_editions.collatex.spike2.MatchSequence;
import com.sd_editions.collatex.spike2.Modification;

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
