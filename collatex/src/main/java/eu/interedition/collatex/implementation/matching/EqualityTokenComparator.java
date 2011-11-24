package eu.interedition.collatex.implementation.matching;

import eu.interedition.collatex.interfaces.INormalizedToken;

import java.util.Comparator;

public class EqualityTokenComparator implements Comparator<INormalizedToken> {

  @Override
  public int compare(INormalizedToken base, INormalizedToken witness) {
    return base.getNormalized().compareTo(witness.getNormalized());
  }

}
