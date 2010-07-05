package eu.interedition.collatex.experimental.ngrams.alignment;

import eu.interedition.collatex.experimental.ngrams.NGram;

public class Gap {
  private final NGram gapA;
  private final NGram gapB;
  private final NGram nextMatchA;

  public Gap(final NGram gapA, final NGram gapB, final NGram nextMatchA) {
    this.gapA = gapA;
    this.gapB = gapB;
    this.nextMatchA = nextMatchA;
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

  public Modification getModification() {
    if (isAddition()) {
      return createAddition();
    }
    if (isOmission()) {
      return createOmission();
    }
    if (isReplacement()) {
      return createReplacement();
    }
    throw new RuntimeException("Not a modification!");
  }

  private Modification createReplacement() {
    return new Replacement(gapA, gapB);
  }

  private Modification createOmission() {
    return new Omission(gapA);
  }

  // TODO 0 -> this is not right!
  private Modification createAddition() {
    return new Addition(nextMatchA, gapB);
  }

}
