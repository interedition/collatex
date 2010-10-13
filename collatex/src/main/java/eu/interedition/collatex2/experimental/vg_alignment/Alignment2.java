package eu.interedition.collatex2.experimental.vg_alignment;

import java.util.List;

import eu.interedition.collatex2.interfaces.ITokenMatch;

public class Alignment2 implements IAlignment2 {

  private final List<ITokenMatch> tokenMatches;

  public Alignment2(List<ITokenMatch> tokenMatches) {
    this.tokenMatches = tokenMatches;
  }

  @Override
  public List<ITokenMatch> getTokenMatches() {
    return tokenMatches;
  }
  

}
