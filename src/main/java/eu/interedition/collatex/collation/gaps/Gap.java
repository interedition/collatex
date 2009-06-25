package eu.interedition.collatex.collation.gaps;

import com.sd_editions.collatex.permutations.collate.Addition;
import com.sd_editions.collatex.permutations.collate.Omission;
import com.sd_editions.collatex.permutations.collate.Replacement;

import eu.interedition.collatex.collation.alignment.Match;
import eu.interedition.collatex.input.Phrase;
import eu.interedition.collatex.visualization.Modification;

public class Gap {
  final Phrase base;
  final Phrase witness;
  final Match next;

  public Gap(Phrase _base, Phrase _witness, Match _next) {
    this.base = _base;
    this.witness = _witness;
    this.next = _next;
  }

  // TODO: rename method -- it does return a Phrase, not a Witness
  public Phrase getBase() {
    return base;
  }

  // TODO: rename method -- it does return a Gap, not a Witness
  public Phrase getWitness() {
    return witness;
  }

  public Addition createAddition() {
    return new Addition(base.getStartPosition(), witness);
  }

  public Omission createOmission() {
    return new Omission(base);
  }

  public Replacement createReplacement() {
    return new Replacement(base, witness);
  }

  public boolean isAddition() {
    return !base.hasGap() && witness.hasGap();
  }

  public boolean isOmission() {
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

  // Note: this the next match after the gap for the second witness!
  // TODO: make defensive!
  public Match getNextMatch() {
    return next;
  }

}
