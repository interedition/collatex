package com.sd_editions.collatex.spike2;

import com.sd_editions.collatex.spike2.collate.Addition;
import com.sd_editions.collatex.spike2.collate.Removal;
import com.sd_editions.collatex.spike2.collate.Replacement;

public class MisMatch {
  final Gap base;
  final Gap witness;

  public MisMatch(Gap _base, Gap _witness) {
    this.base = _base;
    this.witness = _witness;
  }

  public Modification analyse() {
    if (base.hasGap() && witness.hasGap()) {
      return new Replacement(base, witness);
    }
    if (base.hasGap() && !witness.hasGap()) {
      return new Removal(base);
    }
    if (!base.hasGap() && witness.hasGap()) {
      return new Addition(base.getStartPosition(), witness);
    }
    throw new RuntimeException("This mismatch is not valid: there are no modifications!");
  }

  public boolean isValid() {
    return base.hasGap() || witness.hasGap();
  }

}
