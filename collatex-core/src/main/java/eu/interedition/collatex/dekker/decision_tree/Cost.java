package eu.interedition.collatex.dekker.decision_tree;

import eu.interedition.collatex.dekker.matrix.Coordinate;
import eu.interedition.collatex.dekker.matrix.Island;


/*
 * @author: Ronald Haentjens Dekker
 * 
 * Cost represents the associated cost for an edge
 * For the moment we only calculate the distance from the ideal line.
 * The ideal line start on the top left of the match table
 * and goes to the bottom right.
 */

public class Cost {
	public double distanceIdealLine;

	// TODO: This method calculates the distance from the ideal line
  // TODO: by calculating the ratio x/y.
  // TODO: but the ideal line may have moved (due to additions/deletions).
	public Cost(Island isl) {
    Coordinate leftEnd = isl.getLeftEnd();
    double ratio = ((leftEnd.getColumn()+1) / (double) (leftEnd.getRow()+1));
    double b2 = Math.log(ratio)/Math.log(2);
    this.distanceIdealLine = Math.abs(b2);
	}

  public Cost(double distanceIdealLine) {
    this.distanceIdealLine = distanceIdealLine;
  }

  @Override
  public int hashCode() {
    return (int) distanceIdealLine;
  }
  
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Cost)) {
      return false;
    }
    Cost other = (Cost) obj;
    return this.distanceIdealLine == other.distanceIdealLine;
  }
  
  @Override
  public String toString() {
    return new Double(distanceIdealLine).toString();
  }
}
