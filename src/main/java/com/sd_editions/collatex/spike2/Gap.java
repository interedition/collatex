package com.sd_editions.collatex.spike2;

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

}
