package com.sd_editions.collatex.permutations;

import eu.interedition.collatex.input.Word;

public class PMatch {
  final Match match;
  private boolean fixed;

  public PMatch(Match _match) {
    match = _match;
    fixed = false;
  }

  public Word getBaseWord() {
    return match.getBaseWord();
  }

  public Word getWitnessWord() {
    return match.getWitnessWord();
  }

  public void fix() {
    this.fixed = true;
  }

  public boolean isFixed() {
    return fixed;
  }

  @Override
  public String toString() {
    return "[" + getBaseWord().position + "->" + getWitnessWord().position + "]" + (this.isFixed() ? " (fixed)" : " (potential)");
  }

  public float getLevDistance() {
    return match.wordDistance;
  }

  public PMatch copy() {
    PMatch copy = new PMatch(this.match);
    if (this.isFixed()) copy.fix();
    return copy;
  }
}
