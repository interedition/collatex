package eu.interedition.collatex2.experimental.vg_alignment;

import eu.interedition.collatex2.implementation.vg_analysis.IMatch2;


public class Transposition2 implements ITransposition2 {
  private final IMatch2 matchA;
  private final IMatch2 matchB;

  public Transposition2(IMatch2 matchA, IMatch2 matchB) {
    this.matchA = matchA;
    this.matchB = matchB;
  }

  @Override
  public IMatch2 getMatchA() {
    return matchA;
  }

  @Override
  public IMatch2 getMatchB() {
    return matchB;
  }

}
