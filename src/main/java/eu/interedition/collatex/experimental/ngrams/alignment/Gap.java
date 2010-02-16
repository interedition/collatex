package eu.interedition.collatex.experimental.ngrams.alignment;

import eu.interedition.collatex.experimental.ngrams.NGram;

public class Gap {
  public NGram gapA;
  public NGram gapB;

  public Gap(final NGram gapA, final NGram gapB) {
    this.gapA = gapA;
    this.gapB = gapB;
  }

  @Override
  public String toString() {
    return "A: " + gapA.getNormalized() + " -> B: " + gapB.getNormalized();
  }
}
