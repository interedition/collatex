package eu.interedition.collatex.interfaces;

import java.util.Iterator;
import java.util.List;

import eu.interedition.collatex.experimental.ngrams.data.NormalizedToken;

public interface IWitness extends Iterable<NormalizedToken> {

  // Note: not pleased with this method! implement Iterable!
  List<NormalizedToken> getTokens();

  String getSigil();

  // TODO check whether iterator.remove() throws exception!
  Iterator<NormalizedToken> iterator();

  //Note: not pleased with this method! reduce visibility?
  //Note: shouldn't this return a Phrase?
  List<NormalizedToken> getTokens(final int startPosition, final int endPosition);

  public abstract int size();

}