package com.sd_editions.collatex.permutations;

import eu.interedition.collatex.collation.Match;
import eu.interedition.collatex.input.Word;

public class WordDistanceMatch extends Modification {

  private final Match match;

  public WordDistanceMatch(Match _match) {
    this.match = _match;
  }

  public Word base() {
    return match.getBaseWord();
  }

  public Word witness() {
    return match.getWitnessWord();
  }

}
