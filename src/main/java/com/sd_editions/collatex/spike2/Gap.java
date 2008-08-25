package com.sd_editions.collatex.spike2;

public class Gap {
  private final WitnessIndex base;
  private final WitnessIndex witness;
  public int distanceBase;
  public int distanceWitness;
  public int witnessBeginPosition;
  public int witnessEndPosition;
  public int baseBeginPosition;
  public int baseEndPosition;

  public Gap(int baseIndexDif, int witnessIndexDif, WitnessIndex _base, WitnessIndex _witness) {
    this.distanceBase = baseIndexDif;
    this.distanceWitness = witnessIndexDif;
    this.base = _base;
    this.witness = _witness;
  }

  public Phrase createBasePhrase() {
    return base.createPhrase(baseBeginPosition, baseEndPosition);
  }

  public Phrase createWitnessPhrase() {
    return witness.createPhrase(witnessBeginPosition, witnessEndPosition);
  }

}
