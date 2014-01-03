package eu.interedition.collatex.dekker.decision_tree;

/* @author: Ronald Haentjens Dekker
 * Each node in the decision tree has a rank.
 * rank start with 0;
 */
public class DecisionNode {
  private final Integer rank;
  public int linkedTokens = 0;
  
  public DecisionNode(Integer rank) {
    this.rank = rank;
  }
  
  @Override
  public String toString() {
    return "["+rank+"]";
  }
  
  @Override
  public int hashCode() {
    return rank;
  }
  
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof DecisionNode)) {
      return false;
    }
    DecisionNode other = (DecisionNode) obj;
    return other.rank == this.rank;
  }
}
