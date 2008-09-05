package com.sd_editions.collatex.spike2;

public class TranspositionModification {
  private final Phrase base;
  private final Phrase witness;

  public TranspositionModification(Phrase base, Phrase witness) {
    this.base = base;
    this.witness = witness;
  }

  @Override
  public String toString() {
    return "transposition: " + base + " switches position with " + witness;
  }
}
