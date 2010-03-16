package eu.interedition.collatex2.implementation.alignment;

import eu.interedition.collatex2.implementation.modifications.Addition;
import eu.interedition.collatex2.implementation.modifications.Omission;
import eu.interedition.collatex2.implementation.modifications.Replacement;
import eu.interedition.collatex2.interfaces.IGap;
import eu.interedition.collatex2.interfaces.IModification;
import eu.interedition.collatex2.interfaces.IPhrase;

public class Gap implements IGap {
  private final IPhrase gapA;
  private final IPhrase gapB;
  private final IPhrase nextMatchA;

  //TODO: decouple gaps and modifications... 
  //TODO: Modifications should know about gaps not the other way around!
  public Gap(final IPhrase gapA, final IPhrase gapB, final IPhrase nextMatchA) {
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

  public IPhrase getPhraseA() {
    return gapA;
  }

  public IPhrase getPhraseB() {
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

  public IModification getModification() {
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

  private IModification createReplacement() {
    return new Replacement(gapA, gapB);
  }

  private IModification createOmission() {
    return new Omission(gapA);
  }

  // TODO: 0 -> this is not right!
  // TODO: this todo can be removed right?
  private IModification createAddition() {
    return new Addition(nextMatchA, gapB);
  }

}
