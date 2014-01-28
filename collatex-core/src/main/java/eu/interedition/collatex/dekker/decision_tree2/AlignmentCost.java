package eu.interedition.collatex.dekker.decision_tree2;

import com.google.common.base.Objects;

/*
 * Alignment specific cost value object
 * 
 * @author: Ronald Haentjens Dekker
 * 
 */
public class AlignmentCost extends Cost<AlignmentCost> {
  private final int numberOfAlignedVectors;
  private final int numberOfGaps;

  public AlignmentCost() {
    this(0, 0);
  }

  public AlignmentCost(int numberOfAlignedVectors, int numberOfGaps) {
    this.numberOfAlignedVectors = numberOfAlignedVectors;
    this.numberOfGaps = numberOfGaps;
  }

  @Override
  public int compareTo(AlignmentCost o) {
    int result = numberOfGaps - o.numberOfGaps;
    return (result == 0) ? numberOfAlignedVectors - o.numberOfAlignedVectors : result;
  }

  @Override
  AlignmentCost plus(AlignmentCost other) {
    return new AlignmentCost(numberOfGaps + other.numberOfGaps, numberOfAlignedVectors + other.numberOfAlignedVectors);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(numberOfAlignedVectors, numberOfGaps);
  }
  
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof AlignmentCost)) {
      return false;
    }
    AlignmentCost other = (AlignmentCost) obj;
    return numberOfAlignedVectors == other.numberOfAlignedVectors && numberOfGaps == other.numberOfGaps;
  }
}
