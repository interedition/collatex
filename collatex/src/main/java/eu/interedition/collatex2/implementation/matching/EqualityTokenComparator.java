package eu.interedition.collatex2.implementation.matching;

import eu.interedition.collatex2.interfaces.INormalizedToken;

import java.util.Comparator;

public class EqualityTokenComparator implements Comparator<INormalizedToken> {

  @Override
  public int compare(INormalizedToken base, INormalizedToken witness) {
    return base.getNormalized().compareTo(witness.getNormalized());
  }

}
