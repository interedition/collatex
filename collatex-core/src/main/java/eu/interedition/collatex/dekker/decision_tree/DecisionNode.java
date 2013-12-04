package eu.interedition.collatex.dekker.decision_tree;

// @author: Ronald Haentjens Dekker
public class DecisionNode {
  private final Integer id;
  public int linkedTokens = 0;
  
  public DecisionNode(Integer id) {
    this.id = id;
  }
  
  @Override
  public String toString() {
    return "["+id+"]";
  }
}
