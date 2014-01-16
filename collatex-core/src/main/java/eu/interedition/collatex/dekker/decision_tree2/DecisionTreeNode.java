package eu.interedition.collatex.dekker.decision_tree2;

import java.util.List;

import com.google.common.collect.Lists;

import eu.interedition.collatex.dekker.matrix.Island;

/*
 * DecisionTreeNode class
 * 
 * @Author: Ronald Haentjens Dekker
 * 
 * Every node has a fixed number of children.
 * 4 for progressive alignment.
 */
public class DecisionTreeNode {
  private ExtendedMatchTableSelection selection;

  public DecisionTreeNode(ExtendedMatchTableSelection selection) {
    this.selection = selection;
  }

  /*
   * Note: child nodes are recalculated every time.
   * It is expected that the caller will not walk twice over the tree, so that
   * this will not be a problem.
   */
  public List<DecisionTreeNode> getChildNodes() {
    List<DecisionTreeNode> childNodes = Lists.newArrayList();
    // make 4 copies
    ExtendedMatchTableSelection copy1 = new ExtendedMatchTableSelection(selection);
    ExtendedMatchTableSelection copy2 = new ExtendedMatchTableSelection(selection);
    ExtendedMatchTableSelection copy3 = new ExtendedMatchTableSelection(selection);
    ExtendedMatchTableSelection copy4 = new ExtendedMatchTableSelection(selection);
    childNodes.add(copy1.selectFirstVectorFromGraph());
    childNodes.add(copy2.selectFirstVectorFromWitness());
    childNodes.add(copy3.skipFirstVectorFromGraph());
    childNodes.add(copy4.skipFirstVectorFromWitness());
    return childNodes;
  }

  public int getNumberOfAlignedTokens() {
    List<Island> islands = selection.getIslands();
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
    if (selection.getIslands().isEmpty()&&selection.getPossibleIslands().isEmpty()) {
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
}
