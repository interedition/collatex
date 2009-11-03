package com.sd_editions.collatex.permutations;

import eu.interedition.collatex.alignment.Match;
import eu.interedition.collatex.input.Word;
import eu.interedition.collatex.visualization.Modification;

public class WordDistanceMatch extends Modification {

  private final Match<Word> match;

  public WordDistanceMatch(Match<Word> _match) {
    this.match = _match;
  }

  public Word base() {
    return match.getBaseWord();
  }

  public Word witness() {
    return match.getWitnessWord();
  }

}
