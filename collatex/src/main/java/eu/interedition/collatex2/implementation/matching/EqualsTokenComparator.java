package eu.interedition.collatex2.implementation.matching;

import java.util.Comparator;

import eu.interedition.collatex2.interfaces.INormalizedToken;

public class EqualsTokenComparator implements Comparator<INormalizedToken> {

  @Override
  public int compare(INormalizedToken base, INormalizedToken witness) {
    return base.getNormalized().compareTo(witness.getNormalized());
  }

}
