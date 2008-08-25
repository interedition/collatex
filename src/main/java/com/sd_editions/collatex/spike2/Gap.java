package com.sd_editions.collatex.spike2;

import com.sd_editions.collatex.spike2.collate.Addition;
import com.sd_editions.collatex.spike2.collate.Removal;
import com.sd_editions.collatex.spike2.collate.Replacement;

public class Gap {
  final TheRealGap base;
  final TheRealGap witness;

  public Gap(TheRealGap _base, TheRealGap _witness) {
    this.base = _base;
    this.witness = _witness;
  }

  public Modification analyse() {
    if (base.hasGap() && witness.hasGap()) {
      return new Replacement(base.createPhrase(), witness.createPhrase());
    }
    if (base.hasGap() && !witness.hasGap()) {
      return new Removal(base.createPhrase());
    }
    if (!base.hasGap() && witness.hasGap()) {
      return new Addition(base.beginPosition, witness.createPhrase());
    }
    throw new RuntimeException("This gap is empty: there are no modifications!");
  }

  public boolean isEmpty() {
    return !base.hasGap() && !witness.hasGap();
  }

}
