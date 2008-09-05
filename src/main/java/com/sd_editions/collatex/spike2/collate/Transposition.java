package com.sd_editions.collatex.spike2.collate;

import com.sd_editions.collatex.spike2.Modification;
import com.sd_editions.collatex.spike2.Phrase;

public class Transposition extends Modification {
  private final Phrase base;
  private final Phrase witness;

  public Transposition(Phrase base, Phrase witness) {
    this.base = base;
    this.witness = witness;
  }

  @Override
  public String toString() {
    return "transposition: " + base + " switches position with " + witness;
  }

  public String getLeft() {
    return base.toString();
  }

  public String getRight() {
    return witness.toString();
  }
}
