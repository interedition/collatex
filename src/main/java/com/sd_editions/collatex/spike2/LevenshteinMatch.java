package com.sd_editions.collatex.spike2;

public class LevenshteinMatch extends Modification {

  private final Match match;

  public LevenshteinMatch(Match _match) {
    this.match = _match;
  }

  public Word base() {
    return match.getBaseWord();
  }

  public Word witness() {
    return match.getWitnessWord();
  }

}
