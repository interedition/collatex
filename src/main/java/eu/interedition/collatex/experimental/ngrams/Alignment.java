package eu.interedition.collatex.experimental.ngrams;

import java.util.List;

import eu.interedition.collatex.experimental.ngrams.alignment.Gap;

public class Alignment {

  private final List<NGram> matches;
  private final List<Gap> gaps;

  public Alignment(final List<NGram> matches, final List<Gap> gaps) {
    this.matches = matches;
    this.gaps = gaps;
  }

  public List<NGram> getMatches() {
    return matches;
  }

  public List<Gap> getGaps() {
    return gaps;
  }

}
