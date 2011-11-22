package eu.interedition.collatex2.implementation.matching;

import eu.interedition.collatex2.interfaces.INormalizedToken;

public class EqualityTokenComparator extends TokenComparator {

  @Override
  public int compare(INormalizedToken base, INormalizedToken witness) {
    return base.getNormalized().compareTo(witness.getNormalized());
  }

}
