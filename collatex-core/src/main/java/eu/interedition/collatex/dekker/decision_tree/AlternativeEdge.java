package eu.interedition.collatex.dekker.decision_tree;

import eu.interedition.collatex.dekker.matrix.Island;

// @author: Ronald Haentjens Dekker
public class AlternativeEdge {
  private final Island island;
  
  public AlternativeEdge(Island island) {
    this.island = island;
  }
  
  public Island getIsland() {
    return island;
  }

}
