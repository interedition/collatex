package eu.interedition.collatex2.implementation.matching;

import eu.interedition.collatex2.implementation.input.Phrase;
import eu.interedition.collatex2.interfaces.IMatch;
import eu.interedition.collatex2.interfaces.IPhrase;

public class Match implements IMatch {

  private final Phrase a;
  private final Phrase b;

  public Match(final Phrase a, final Phrase b) {
    this.a = a;
    this.b = b;
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
