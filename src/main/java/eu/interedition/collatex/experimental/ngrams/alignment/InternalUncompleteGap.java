package eu.interedition.collatex.experimental.ngrams.alignment;

import eu.interedition.collatex.experimental.ngrams.NGram;

public class InternalUncompleteGap {
  private final NGram gapA;
  private final NGram gapB;

  public InternalUncompleteGap(final NGram gapA, final NGram gapB) {
    this.gapA = gapA;
    this.gapB = gapB;
  }

  @Override
  public String toString() {
    if (isAddition()) {
      return "\"" + gapB.getNormalized() + "\" added";
    }
    return "A: " + gapA.getNormalized() + " -> B: " + gapB.getNormalized();
  }

  public NGram getNGramA() {
    return gapA;
  }

  public NGram getNGramB() {
    return gapB;
  }

  public boolean isEmpty() {
    return gapA.isEmpty() && gapB.isEmpty();
  }

  public boolean isReplacement() {
    return !gapA.isEmpty() && !gapB.isEmpty();
  }

  public boolean isAddition() {
    return gapA.isEmpty() && !gapB.isEmpty();
  }

  private boolean isOmission() {
    return !gapA.isEmpty() && gapB.isEmpty();
  }

}
