package eu.interedition.collatex2.implementation.matching;

import java.util.Comparator;

import eu.interedition.collatex2.interfaces.INormalizedToken;

public class NearTokenComparator implements Comparator<INormalizedToken> {

  @Override
  public int compare(INormalizedToken o1, INormalizedToken o2) {
    int editDistance = EditDistance.compute(o1.getNormalized(), o2.getNormalized());
    if (editDistance == 0 || editDistance == 1) {
      return 1;
    }
    return -1;
  }
}
