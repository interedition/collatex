package eu.interedition.collatex2.implementation.vg_alignment;

import eu.interedition.collatex2.implementation.vg_analysis.ISequence;
import eu.interedition.collatex2.interfaces.IPhrase;

public class Sequence implements ISequence {

  private final IPhrase basePhrase;
  private final IPhrase witnessPhrase;

  public Sequence(IPhrase basePhrase, IPhrase witnessPhrase) {
    this.basePhrase = basePhrase;
    this.witnessPhrase = witnessPhrase;
  }

  //TODO: Delete method!
  public IPhrase getTablePhrase() {
    return basePhrase;
  }

  //TODO: Delete method!
  public IPhrase getPhrase() {
    return witnessPhrase;
  }

  @Override
  public String toString() {
    return basePhrase.getNormalized() + " -> "+witnessPhrase.getNormalized();
  }

  @Override
  public String getNormalized() {
    return witnessPhrase.getNormalized();
  }

  @Override
  public IPhrase getBasePhrase() {
    return basePhrase;
  }

  @Override
  public IPhrase getWitnessPhrase() {
    return witnessPhrase;
  }
}
