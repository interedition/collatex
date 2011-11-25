package eu.interedition.collatex.implementation.matching;

import eu.interedition.collatex.interfaces.INormalizedToken;

import java.util.Comparator;

public class EditDistanceTokenComparator implements Comparator<INormalizedToken> {

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
