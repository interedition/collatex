package eu.interedition.collatex.experimental.ngrams;

import java.util.List;

public class Alignment {

  private final List<NGram> matches;

  public Alignment(final List<NGram> matches) {
    this.matches = matches;
  }

  public List<NGram> getMatches() {
    return matches;
  }

}
