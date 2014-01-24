package eu.interedition.collatex.dekker.decision_tree2;

import java.util.List;
import java.util.Set;

import eu.interedition.collatex.dekker.matrix.Island;

/*
 * DecisionTreeNode class
 * 
 * @Author: Ronald Haentjens Dekker
 * 
 * Every node has a fixed number of children (in case of progressive alignment).
 * 1 child (no further possibilities), 2 (aligned) or 4 (transposition) children.
 * 
 */
public class DecisionTreeNode {
  ExtendedMatchTableSelection selection;

  public DecisionTreeNode(ExtendedMatchTableSelection selection) {
    this.selection = selection;
  }


  public int getNumberOfAlignedTokens() {
    List<Island> islands = selection.getIslands();
    int numberOfSelectedTokens = 0;
    for (Island selected : islands) {
      numberOfSelectedTokens += selected.size();
    }
    return numberOfSelectedTokens;
  }

  public int getNumberOfTransposedTokens() {
    Set<Island> islands = selection.getTransposedIslands();
    int numberOfSelectedTokens = 0;
    for (Island selected : islands) {
      numberOfSelectedTokens += selected.size();
    }
    return numberOfSelectedTokens;
  }

  //TODO: this implementation is too simple
  //TODO: one should take the delta since last aligned token into account
  public int getNumberOfGapTokens() {
    if (selection.getIslands().isEmpty()&&!selection.skippedIslands) {
      return 0;
    }
    if (selection.getPossibleIslands().isEmpty()) {
      //TODO: the size of the witness can be greater than the size
      //TODO: of the graph (Math.max etc.)
      int width = selection.sizeOfGraph();
      return width - getNumberOfAlignedTokens();
    } else {
      Island nextIsland = selection.getFirstVectorFromGraph();
      int column = nextIsland.getLeftEnd().getColumn();
      return column - getNumberOfAlignedTokens();
    }
  }

  public int getNumberOfSelectedVectors() {
    return selection.getIslands().size();
  }

  public boolean hasSkippedIslands() {
    return selection.skippedIslands;
  }

  public boolean isFinished() {
    return selection.getPossibleIslands().isEmpty();
  }

  public String log() {
    return selection.log();
  }

  public List<Island> getIslands() {
    return selection.getIslands();
  }
  
  @Override
  public int hashCode() {
    return selection.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof DecisionTreeNode)) {
      return false;
    }
    DecisionTreeNode other = (DecisionTreeNode) obj;
    return selection == other.selection;
  }
}
