package eu.interedition.collatex.collation;

import com.sd_editions.collatex.permutations.Gap;
import com.sd_editions.collatex.permutations.Modification;
import com.sd_editions.collatex.permutations.collate.Addition;
import com.sd_editions.collatex.permutations.collate.Omission;
import com.sd_editions.collatex.permutations.collate.Replacement;

public class NonMatch {
  final Gap base;
  final Gap witness;

  public Gap getBase() {
    return base;
  }

  public Gap getWitness() {
    return witness;
  }

  public NonMatch(Gap _base, Gap _witness) {
    this.base = _base;
    this.witness = _witness;
  }

  Addition createAddition() {
    return new Addition(base.getStartPosition(), witness);
  }

  Omission createOmission() {
    return new Omission(base);
  }

  Replacement createReplacement() {
    return new Replacement(base, witness);
  }

  public boolean isAddition() {
    return !base.hasGap() && witness.hasGap();
  }

  boolean isOmission() {
    return base.hasGap() && !witness.hasGap();
  }

  public boolean isReplacement() {
    return base.hasGap() && witness.hasGap();
  }

  public boolean isValid() {
    return base.hasGap() || witness.hasGap();
  }

  @Override
  public String toString() {
    String result = "NonMatch: addition: " + isAddition() + " base: " + base;
    if (base.isAtTheEnd()) {
      result += "; nextWord: none";
    } else {
      result += "; nextWord: " + base.getNextWord();
    }
    result += "; witness: " + witness;
    return result;
  }

  public Modification analyse() {
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

}
