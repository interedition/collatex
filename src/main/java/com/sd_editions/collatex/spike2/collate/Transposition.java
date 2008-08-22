package com.sd_editions.collatex.spike2.collate;

import com.sd_editions.collatex.spike2.Modification;

public class Transposition extends Modification {
  private final String transposedWord;
  private final int transpositionDistance;

  public Transposition(String word, int _transpositionDistance) {
    this.transposedWord = word;
    this.transpositionDistance = _transpositionDistance;
  }

  public String getTransposedWord() {
    return transposedWord;
  }

  public int getTranspositionDistance() {
    return transpositionDistance;
  }

  @Override
  public String toString() {
    return "transposition: " + getTransposedWord() + " distance: " + getTranspositionDistance();
  }
}
