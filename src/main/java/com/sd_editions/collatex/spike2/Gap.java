package com.sd_editions.collatex.spike2;

public class Gap {
  private final WitnessIndex witness;
  private final int size;
  final int beginPosition;
  final int endPosition;

  public Gap(WitnessIndex _witness, int _size, int _beginPosition, int _endPosition) {
    this.witness = _witness;
    this.size = _size;
    this.beginPosition = _beginPosition;
    this.endPosition = _endPosition;
  }

  public Phrase createPhrase() {
    return witness.createPhrase(beginPosition, endPosition);
  }

  public boolean hasGap() {
    return size > 0;
  }

}
