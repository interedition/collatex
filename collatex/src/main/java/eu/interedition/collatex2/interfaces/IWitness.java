package eu.interedition.collatex2.interfaces;

import java.util.List;

public interface IWitness extends ITokenContainer {

  // Note: not pleased with this method! implement Iterable!
  List<INormalizedToken> getTokens();

  IPhrase createPhrase(final int startPosition, final int endPosition);

  int size();

  String getSigil();

}
