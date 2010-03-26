package eu.interedition.collatex2.implementation.matching;

import eu.interedition.collatex2.interfaces.IMatch;
import eu.interedition.collatex2.interfaces.IPhrase;

public class Match implements IMatch {

  private final IPhrase a;
  private final IPhrase b;

  public Match(final IPhrase basePhrase, final IPhrase witnessPhrase) {
    this.a = basePhrase;
    this.b = witnessPhrase;
  }

  @Override
  public String getNormalized() {
    return a.getNormalized();
  }

  @Override
  public IPhrase getPhraseA() {
    return a;
  }

  @Override
  public IPhrase getPhraseB() {
    return b;
  }

  @Override
  public String toString() {
    return getPhraseA() + "->" + getPhraseB();
  }
}
