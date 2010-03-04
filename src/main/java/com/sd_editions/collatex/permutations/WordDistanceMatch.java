package com.sd_editions.collatex.permutations;

import eu.interedition.collatex.alignment.Match;
import eu.interedition.collatex.experimental.ngrams.alignment.Modification;
import eu.interedition.collatex.input.BaseElement;

public class WordDistanceMatch<T extends BaseElement> extends Modification {

  private final Match<T> match;

  public WordDistanceMatch(final Match<T> _match) {
    this.match = _match;
  }

  public T base() {
    return match.getBaseWord();
  }

  public T witness() {
    return match.getWitnessWord();
  }

}
