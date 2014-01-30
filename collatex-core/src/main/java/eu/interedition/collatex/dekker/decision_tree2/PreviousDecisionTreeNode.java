package eu.interedition.collatex.dekker.decision_tree2;

import java.util.List;

import eu.interedition.collatex.dekker.matrix.Island;

/*
 * TO BE DELETED!
 * DecisionTreeNode class
 * 
 * @Author: Ronald Haentjens Dekker
 * 
 * Every node has a fixed number of children (in case of progressive alignment).
 * 1 child (no further possibilities), 2 (aligned) or 4 (transposition) children.
 * 
 */
public class PreviousDecisionTreeNode extends DecisionTreeNode {
  ExtendedMatchTableSelection selection;

  public PreviousDecisionTreeNode(ExtendedMatchTableSelection selection) {
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
    return 0;
//    Set<Island> islands = selection.getTransposedIslands();
//    int numberOfSelectedTokens = 0;
//    for (Island selected : islands) {
//      numberOfSelectedTokens += selected.size();
//    }
//    return numberOfSelectedTokens;
  }

  //TODO: this implementation is too simple
  //TODO: one should take the delta since last aligned token into account
  public int getNumberOfGapTokens() {
    if (selection.getIslands().isEmpty()/*&&!selection.skippedIslands*/) {
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
    return false;
  }

  public String log() {
    return "no log";
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
    if (!(obj instanceof PreviousDecisionTreeNode)) {
      return false;
    }
    PreviousDecisionTreeNode other = (PreviousDecisionTreeNode) obj;
    return selection == other.selection;
  }


  public boolean isFinished() {
    return selection.isFinished();
  }
}
