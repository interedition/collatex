package eu.interedition.collatex.dekker.decision_tree2;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.dekker.astar.AstarAlgorithm;
import eu.interedition.collatex.dekker.matrix.Island;

/*
 * Alignment algorithm based on a decision tree using the a* algorithm. 
 * with a fixed number of children per node.
 * 
 * @author: Ronald Haentjens Dekker
 */

public class DekkerDecisionTreeAlgorithm extends AstarAlgorithm<DecisionTreeNode, AlignmentCost> {
  final Map<DecisionTreeNode, ExtendedMatchTableSelection> selection;
  
  public DekkerDecisionTreeAlgorithm() {
    this.selection = Maps.newHashMap();
  }
  
  @Override
  public void collate(VariantGraph against, Iterable<Token> witness) {
    // TODO Auto-generated method stub
    
  }

  void associate(DecisionTreeNode node, ExtendedMatchTableSelection _selection) {
    selection.put(node, _selection);
  }

  @Override
  protected boolean isGoal(DecisionTreeNode node) {
    return selection.get(node).isFinished();
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
  protected Set<DecisionTreeNode> neighborNodes(DecisionTreeNode current) {
    //TODO: change next line; should be no neighbors
    //TODO: to test
    if (selection.get(current).isFinished()) {
      return Collections.singleton(current);
    }
    Island firstVectorGraph = selection.get(current).getFirstVectorFromGraph();
    Island firstVectorWitness = selection.get(current).getFirstVectorFromWitness();
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
    DecisionTreeNode child = new DecisionTreeNode();
    ExtendedMatchTableSelection childSelection = selection.get(current).copy(); 
    selection.put(child, childSelection);
    childNodes.add(child);
    return childSelection;
  }

  @Override
  protected AlignmentCost heuristicCostEstimate(DecisionTreeNode node) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  protected AlignmentCost distBetween(DecisionTreeNode current, DecisionTreeNode neighbor) {
    // TODO Auto-generated method stub
    return null;
  }

}
