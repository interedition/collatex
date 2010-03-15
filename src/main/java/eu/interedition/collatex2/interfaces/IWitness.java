package eu.interedition.collatex2.interfaces;

import java.util.List;

public interface IWitness {

  // Note: not pleased with this method! implement Iterable!
  List<INormalizedToken> getTokens();

  INGram createNGram(final int startPosition, final int endPosition);

  int size();

}
