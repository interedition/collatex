package eu.interedition.collatex2.implementation.matching;

import java.util.Comparator;

import eu.interedition.collatex2.interfaces.INormalizedToken;

public class EditDistanceTokenComparator extends TokenMatcher {

  private final int threshold;

  public EditDistanceTokenComparator() {
    this(1);
  }

  public EditDistanceTokenComparator(int threshold) {
    this.threshold = threshold;
  }

  @Override
  public int compare(INormalizedToken o1, INormalizedToken o2) {
    return (EditDistance.compute(o1.getNormalized(), o2.getNormalized()) <= threshold) ? 0 : -1;
  }
}
