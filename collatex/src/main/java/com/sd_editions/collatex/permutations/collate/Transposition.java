package com.sd_editions.collatex.permutations.collate;

import eu.interedition.collatex.alignment.Match;
import eu.interedition.collatex.alignment.MatchSequence;
import eu.interedition.collatex.experimental.ngrams.alignment.Modification;
import eu.interedition.collatex.input.BaseElement;

//TODO make generic!
public class Transposition extends Modification {
  private final MatchSequence base;
  private final MatchSequence witness;
  private final Match<BaseElement> _nextMatch;

  public Transposition(final MatchSequence _base, final MatchSequence _witness, final Match nextMatch) {
    this.base = _base;
    this.witness = _witness;
    this._nextMatch = nextMatch;
  }

  @Override
  public String toString() {
    return "transposition: " + getLeft() + " switches position with " + getRight();
  }

  public String getLeft() {
    return base.baseToString();
  }

  public String getRight() {
    return witness.baseToString();
  }

  public MatchSequence getBase() {
    return base;
  }

  public MatchSequence getWitness() {
    return witness;
  }

  public Match getNextMatch() {
    return _nextMatch;
  }
}
