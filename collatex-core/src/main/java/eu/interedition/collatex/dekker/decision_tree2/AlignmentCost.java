package eu.interedition.collatex.dekker.decision_tree2;

import com.google.common.base.Objects;

import eu.interedition.collatex.dekker.astar.Cost;

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
  protected AlignmentCost plus(AlignmentCost other) {
    return new AlignmentCost(numberOfAlignedVectors + other.numberOfAlignedVectors, numberOfGaps + other.numberOfGaps);
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
  
  @Override
  public String toString() {
    return String.format("Number of aligned vectors: %s; number of gaps: %s", numberOfAlignedVectors, numberOfGaps);
  }
}
