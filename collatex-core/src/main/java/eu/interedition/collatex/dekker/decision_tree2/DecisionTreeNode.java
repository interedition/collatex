package eu.interedition.collatex.dekker.decision_tree2;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;

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
  private ExtendedMatchTableSelection selection;

  public DecisionTreeNode(ExtendedMatchTableSelection selection) {
    this.selection = selection;
  }

  // There are three situations:
  // 1. There are no unallocated vectors, return this node.
  // 2. The first vector of the graph and the witness
  //    are the same. Return two nodes:
  //    1. Select the vector.
  // or 2. Skip the vector.
  // 3. The first vector of the graph and the witness
  //    are different. Return 4 nodes.
  /*
   * Note: child nodes are recalculated every time.
   * It is expected that the caller will not walk twice over the tree, so that
   * this will not be a problem.
   */
  public List<DecisionTreeNode> getChildNodes() {
    if (selection.getPossibleIslands().isEmpty()) {
      return Collections.singletonList(this);
    }
    Island firstVectorGraph = selection.getFirstVectorFromGraph();
    Island firstVectorWitness = selection.getFirstVectorFromWitness();
    List<DecisionTreeNode> childNodes = Lists.newArrayList();
    if (firstVectorGraph!=firstVectorWitness) {
      // make 4 copies
      ExtendedMatchTableSelection copy1 = new ExtendedMatchTableSelection(selection);
      ExtendedMatchTableSelection copy2 = new ExtendedMatchTableSelection(selection);
      ExtendedMatchTableSelection copy3 = new ExtendedMatchTableSelection(selection);
      ExtendedMatchTableSelection copy4 = new ExtendedMatchTableSelection(selection);
      childNodes.add(copy1.selectFirstVectorGraphTransposeWitness());
      childNodes.add(copy2.selectFirstVectorWitnessTransposeGraph());
      childNodes.add(copy3.skipFirstVectorFromGraph());
      childNodes.add(copy4.skipFirstVectorFromWitness());
    } else {
      // make 2 copies
      ExtendedMatchTableSelection copy1 = new ExtendedMatchTableSelection(selection);
      ExtendedMatchTableSelection copy2 = new ExtendedMatchTableSelection(selection);
      childNodes.add(copy1.selectFirstVectorFromGraph());
      childNodes.add(copy2.skipFirstVectorFromGraph());
    }
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

}
