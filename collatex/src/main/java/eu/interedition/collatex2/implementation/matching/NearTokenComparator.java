package eu.interedition.collatex2.implementation.matching;

import java.util.Comparator;

import eu.interedition.collatex2.interfaces.INormalizedToken;

public class NearTokenComparator implements Comparator<INormalizedToken> {

  @Override
  public int compare(INormalizedToken o1, INormalizedToken o2) {
    final int editDistance = EditDistance.compute(o1.getNormalized(), o2.getNormalized());
    return (editDistance == 0 || editDistance == 1) ? 0 : -1;
  }
}
