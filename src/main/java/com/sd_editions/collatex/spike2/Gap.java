package com.sd_editions.collatex.spike2;

public class Gap {
  public int distanceBase;
  public int distanceWitness;
  public int witnessBeginPosition;
  public int witnessEndPosition;
  public int baseBeginPosition;
  public int baseEndPosition;

  public Gap(int baseIndexDif, int witnessIndexDif) {
    this.distanceBase = baseIndexDif;
    this.distanceWitness = witnessIndexDif;
  }

}
