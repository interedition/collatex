package com.sd_editions.collatex.spike2;

import com.sd_editions.collatex.spike2.collate.Addition;
import com.sd_editions.collatex.spike2.collate.Removal;
import com.sd_editions.collatex.spike2.collate.Replacement;

public class Gap {
  private final WitnessIndex base;
  private final WitnessIndex witness;
  private final int sizeInBase;
  private final int sizeInWitness;
  public int witnessBeginPosition;
  public int witnessEndPosition;
  public int baseBeginPosition;
  public int baseEndPosition;

  public Gap(int baseIndexDif, int witnessIndexDif, WitnessIndex _base, WitnessIndex _witness) {
    this.sizeInBase = baseIndexDif;
    this.sizeInWitness = witnessIndexDif;
    this.base = _base;
    this.witness = _witness;
  }

  public Phrase createBasePhrase() {
    return base.createPhrase(baseBeginPosition, baseEndPosition);
  }

  public Phrase createWitnessPhrase() {
    return witness.createPhrase(witnessBeginPosition, witnessEndPosition);
  }

  public boolean gapInBase() {
    return sizeInBase > 0;
  }

  public boolean gapInWitness() {
    return sizeInWitness > 0;
  }

  public Modification analyse() {
    if (gapInBase() && gapInWitness()) {
      return new Replacement(createBasePhrase(), createWitnessPhrase());
    }
    if (gapInBase() && !gapInWitness()) {
      return new Removal(createBasePhrase());
    }
    if (!gapInBase() && gapInWitness()) {
      return new Addition(baseBeginPosition, createWitnessPhrase());
    }
    throw new RuntimeException("This gap is empty: there are no modifications!");
  }

  public boolean isEmpty() {
    return !gapInBase() && !gapInWitness();
  }

}
