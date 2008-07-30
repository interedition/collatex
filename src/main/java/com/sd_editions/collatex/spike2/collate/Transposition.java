package com.sd_editions.collatex.spike2.collate;

import com.sd_editions.collatex.spike2.Modification;

public class Transposition extends Modification {
  private final int transposedWord;
  private final int transpositionDistance;

  public Transposition(int _transposedWord, int _transpositionDistance) {
    this.transposedWord = _transposedWord;
    this.transpositionDistance = _transpositionDistance;
  }

  public int getTransposedWord() {
    return transposedWord;
  }

  public int getTranspositionDistance() {
    return transpositionDistance;
  }
}
