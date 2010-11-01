package eu.interedition.collatex2.implementation.vg_alignment;

import eu.interedition.collatex2.implementation.vg_analysis.ISequence;
import eu.interedition.collatex2.interfaces.IPhrase;

public class PhraseMatch implements ISequence {

  private final IPhrase basePhrase;
  private final IPhrase witnessPhrase;

  public PhraseMatch(IPhrase basePhrase, IPhrase witnessPhrase) {
    this.basePhrase = basePhrase;
    this.witnessPhrase = witnessPhrase;
  }

  public IPhrase getTablePhrase() {
    return basePhrase;
  }

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
  public IPhrase getPhraseA() {
    return basePhrase;
  }

  @Override
  public IPhrase getPhraseB() {
    return witnessPhrase;
  }
}
