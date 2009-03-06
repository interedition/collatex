package com.sd_editions.collatex.permutations;

public class Gap extends Phrase {
  private final int size;

  public Gap(Witness _witness, int _size, int _beginPosition, int _endPosition) {
    super(_witness, _beginPosition, _endPosition);
    this.size = _size;
  }

  public boolean hasGap() {
    return size > 0;
  }

}
