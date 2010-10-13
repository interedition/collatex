package eu.interedition.collatex2.experimental.vg_alignment;

import eu.interedition.collatex2.implementation.vg_analysis.ISequence;


public class Transposition2 implements ITransposition2 {
  private final ISequence matchA;
  private final ISequence matchB;

  public Transposition2(ISequence matchA, ISequence matchB) {
    this.matchA = matchA;
    this.matchB = matchB;
  }

  @Override
  public ISequence getMatchA() {
    return matchA;
  }

  @Override
  public ISequence getMatchB() {
    return matchB;
  }

}
