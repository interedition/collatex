package eu.interedition.collatex.dekker.decision_tree2;

import java.util.Collections;
import java.util.Set;

import com.google.common.collect.Sets;

import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.dekker.matrix.Island;

/*
 * Alignment algorithm based on a decision tree using the a* algorithm. 
 * with a fixed number of children per node.
 * 
 * @author: Ronald Haentjens Dekker
 */

public class DekkerDecisionTreeAlgorithm extends AstarAlgorithm {

  @Override
  public void collate(VariantGraph against, Iterable<Token> witness) {
    // TODO Auto-generated method stub
    
  }

  @Override
  boolean isGoal(DecisionTreeNode node) {
    return node.isFinished();
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
  @Override
  Set<DecisionTreeNode> neighborNodes(DecisionTreeNode current) {
    //TODO: change next line; should be no neighbors
    //TODO: to test
    if (current.selection.getPossibleIslands().isEmpty()) {
      return Collections.singleton(current);
    }
    Island firstVectorGraph = current.selection.getFirstVectorFromGraph();
    Island firstVectorWitness = current.selection.getFirstVectorFromWitness();
    Set<DecisionTreeNode> childNodes = Sets.newHashSet();
    if (firstVectorGraph!=firstVectorWitness) {
      // make 4 copies
      addNode(childNodes, current).selectFirstVectorGraphTransposeWitness();
      addNode(childNodes, current).selectFirstVectorWitnessTransposeGraph();
      addNode(childNodes, current).skipFirstVectorFromGraph();
      addNode(childNodes, current).skipFirstVectorFromWitness();
    } else {
      // make 2 copies
      addNode(childNodes, current).selectFirstVectorFromGraph();
      addNode(childNodes, current).skipFirstVectorFromGraph();
    }
    return childNodes;
  }

  private ExtendedMatchTableSelection addNode(Set<DecisionTreeNode> childNodes, DecisionTreeNode current) {
    ExtendedMatchTableSelection childSelection = current.selection.copy(); 
    childNodes.add(new DecisionTreeNode(childSelection));
    return childSelection;
  }

  @Override
  Integer heuristicCostEstimate(DecisionTreeNode node) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  Integer distBetween(DecisionTreeNode current, DecisionTreeNode neighbor) {
    // TODO Auto-generated method stub
    return null;
  }

}
