package eu.interedition.collatex2.implementation.vg_analysis;



public class Transposition2 implements ITransposition2 {
  private final ISequence sequenceA;
  private final ISequence sequenceB;

  public Transposition2(ISequence sequenceA, ISequence sequenceB) {
    this.sequenceA = sequenceA;
    this.sequenceB = sequenceB;
  }

  @Override
  public ISequence getSequenceA() {
    return sequenceA;
  }

  @Override
  public ISequence getSequenceB() {
    return sequenceB;
  }
  
  @Override
  public String toString() {
    return sequenceA.toString() + " -> "+sequenceB.toString();
  }

}
