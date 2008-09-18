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

  Addition createAddition() {
    return new Addition(base.getStartPosition(), witness);
  }

  Removal createOmission() {
    return new Removal(base);
  }

  Replacement createReplacement() {
    return new Replacement(base, witness);
  }

  boolean isAddition() {
    return !base.hasGap() && witness.hasGap();
  }

  boolean isOmission() {
    return base.hasGap() && !witness.hasGap();
  }

  boolean isReplacement() {
    return base.hasGap() && witness.hasGap();
  }

  public boolean isValid() {
    return base.hasGap() || witness.hasGap();
  }

  public Modification analyse() {
    if (isAddition()) {
      return createAddition();
    }
    if (isOmission()) {
      return createOmission();
    }
    if (isReplacement()) {
      return createReplacement();
    }
    throw new RuntimeException("Not a modification!");
  }

}
