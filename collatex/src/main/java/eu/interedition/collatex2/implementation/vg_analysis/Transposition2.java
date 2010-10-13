package eu.interedition.collatex2.implementation.vg_analysis;



public class Transposition2 implements ITransposition2 {
  private final ISequence matchA;
  private final ISequence matchB;

  public Transposition2(ISequence matchA, ISequence matchB) {
    this.matchA = matchA;
    this.matchB = matchB;
  }

  @Override
  public ISequence getSequenceA() {
    return matchA;
  }

  @Override
  public ISequence getSequenceB() {
    return matchB;
  }

}
