package eu.interedition.collatex2.implementation.matching;

import eu.interedition.collatex2.interfaces.IPhrase;
import eu.interedition.collatex2.interfaces.IPhraseMatch;

public class PhraseMatch implements IPhraseMatch {

  private final IPhrase a;
  private final IPhrase b;

  public PhraseMatch(final IPhrase a, final IPhrase b) {
    this.a = a;
    this.b = b;
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
  public String getNormalized() {
    return a.getNormalized();
  }

}
