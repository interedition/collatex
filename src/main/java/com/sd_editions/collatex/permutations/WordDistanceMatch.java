package com.sd_editions.collatex.permutations;

import eu.interedition.collatex.alignment.Match;
import eu.interedition.collatex.input.BaseElement;
import eu.interedition.collatex.visualization.Modification;

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
