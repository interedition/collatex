package eu.interedition.collatex2.implementation.matching;

import eu.interedition.collatex2.interfaces.IColumns;
import eu.interedition.collatex2.interfaces.IMatch;
import eu.interedition.collatex2.interfaces.IPhrase;

public class Match implements IMatch {

  private final IColumns a;
  private final IPhrase b;

  public Match(final IColumns columnsA, final IPhrase witnessPhrase) {
    this.a = columnsA;
    this.b = witnessPhrase;
  }

  @Override
  public String getNormalized() {
    return b.getNormalized();
  }

  @Override
  public IColumns getColumnsA() {
    return a;
  }

  @Override
  public IPhrase getPhraseB() {
    return b;
  }

  @Override
  public String toString() {
    return getColumnsA() + "->" + getPhraseB();
  }
}
