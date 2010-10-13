package eu.interedition.collatex2.implementation.tokenmatching;

import eu.interedition.collatex2.implementation.vg_analysis.IMatch2;
import eu.interedition.collatex2.interfaces.IPhrase;

public class PhraseMatch implements IMatch2 {

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
    return basePhrase.getContent() + " -> "+witnessPhrase.getContent();
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
