package eu.interedition.collatex2.implementation.matching;

import eu.interedition.collatex2.implementation.indexing.NGram;
import eu.interedition.collatex2.interfaces.IMatch;
import eu.interedition.collatex2.interfaces.INGram;

public class Match implements IMatch {

  private final NGram a;
  private final NGram b;

  public Match(final NGram a, final NGram b) {
    this.a = a;
    this.b = b;
  }

  @Override
  public String getNormalized() {
    return a.getNormalized();
  }

  @Override
  public INGram getNGramA() {
    return a;
  }

  @Override
  public INGram getNGramB() {
    return b;
  }

  @Override
  public String toString() {
    return getNGramA() + "->" + getNGramB();
  }
}
