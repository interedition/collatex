package eu.interedition.collatex.dekker.decision_tree;

import eu.interedition.collatex.dekker.matrix.Island;

// @author: Ronald Haentjens Dekker
public class AlternativeEdge {
  private final Island island;
  private DecisionTree tree;
  
  public AlternativeEdge(DecisionTree tree, Island island) {
    this.tree = tree;
    this.island = island;
  }
  
  public Island getIsland() {
    return island;
  }

  @Override
  public String toString() {
    DecisionNode source = tree.getSource(this);
    DecisionNode dest = tree.getDest(this);
    return source+"-["+(island.getLeftEnd().getColumn()+1)+","+(island.getLeftEnd().getRow()+1)+",l:"+island.size()+"]->"+dest;
  }
}
