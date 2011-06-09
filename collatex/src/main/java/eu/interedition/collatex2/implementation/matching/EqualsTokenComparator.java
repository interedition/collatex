package eu.interedition.collatex2.implementation.matching;

import java.util.Comparator;

import eu.interedition.collatex2.interfaces.INormalizedToken;

public class EqualsTokenComparator implements Comparator<INormalizedToken> {

  @Override
  public int compare(INormalizedToken base, INormalizedToken witness) {
    if (base.getNormalized().equals(witness.getNormalized())) {
      return 1;
    }
    return -1;
  }

}
