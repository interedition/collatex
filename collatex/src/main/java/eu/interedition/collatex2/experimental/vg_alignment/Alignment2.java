package eu.interedition.collatex2.experimental.vg_alignment;

import java.util.List;

public class Alignment2 implements IAlignment2 {

  private final List<IMatch2> matches;

  public Alignment2(List<IMatch2> matches) {
    this.matches = matches;
  }

  @Override
  public List<IMatch2> getMatches() {
    return matches;
  }

}
