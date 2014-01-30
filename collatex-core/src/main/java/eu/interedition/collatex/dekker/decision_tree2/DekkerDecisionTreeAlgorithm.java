package eu.interedition.collatex.dekker.decision_tree2;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
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
  // temporary measure to track the results
  private final List<String> log;
  
  public DekkerDecisionTreeAlgorithm() {
    this.selection = Maps.newHashMap();
    this.log = Lists.newArrayList();
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
      DecisionTreeNode addNode = addNode(childNodes, current);
      selectFirstVectorGraphTransposeWitness(addNode, firstVectorGraph, firstVectorWitness);
      DecisionTreeNode addNode2 = addNode(childNodes, current);
      selectFirstVectorWitnessTransposeGraph(addNode2, firstVectorGraph, firstVectorWitness);
      DecisionTreeNode addNode3 = addNode(childNodes, current);
      skipFirstVectorFromGraph(addNode3, firstVectorGraph);
      DecisionTreeNode addNode4 = addNode(childNodes, current);
      skipFirstVectorFromWitness(addNode4, firstVectorWitness);
    } else {
      // make 2 copies
      DecisionTreeNode addNode = addNode(childNodes, current);
      selectFirstVectorFromGraph(addNode, firstVectorGraph);
      DecisionTreeNode addNode2 = addNode(childNodes, current);
      skipFirstVectorFromGraph(addNode2, firstVectorGraph);
    }
    return childNodes;
  } 

  private DecisionTreeNode addNode(Set<DecisionTreeNode> childNodes, DecisionTreeNode current) {
    DecisionTreeNode child = new DecisionTreeNode();
    ExtendedMatchTableSelection childSelection = selection.get(current).copy();
    selection.put(child, childSelection);
    childNodes.add(child);
    return child;
  }
  
  private void skipFirstVectorFromGraph(DecisionTreeNode node, Island firstVectorGraph) {
    ExtendedMatchTableSelection sel = selection.get(node);
    sel.removeIslandFromPossibilities(firstVectorGraph);
    log.add(String.format("skip g %s", firstVectorGraph));
  }

  private void skipFirstVectorFromWitness(DecisionTreeNode node, Island firstVectorWitness) {
    ExtendedMatchTableSelection sel = selection.get(node);
    sel.removeIslandFromPossibilities(firstVectorWitness);
    log.add(String.format("skip g %s", firstVectorWitness));
  }

  private void selectFirstVectorFromGraph(DecisionTreeNode node, Island i) {
    ExtendedMatchTableSelection sel = selection.get(node);
    sel.selectIsland(i);
    node.setLastSelected(i);
    log.add(String.format("sel g %s", i));
  }

  private void selectFirstVectorGraphTransposeWitness(DecisionTreeNode node, Island firstVectorGraph, Island firstVectorWitness) {
    ExtendedMatchTableSelection sel = selection.get(node);
    // Find that vector in the witness
    // as long as you can't find that vector
    // transpose the vectors in the witness
    Island witness = firstVectorWitness;
    do {
      log.add(String.format("transposed w %s", witness));
      node.addTransposed(witness);
      sel.removeIslandFromPossibilities(witness);
      witness = sel.getFirstVectorFromWitness();
    } while (witness != firstVectorGraph);
    node.setLastSelected(firstVectorGraph);
    sel.selectIsland(firstVectorGraph);
  }
  
  private void selectFirstVectorWitnessTransposeGraph(DecisionTreeNode node, Island firstVectorGraph, Island firstVectorWitness) {
    ExtendedMatchTableSelection sel = selection.get(node);
    // Find that vector in the graph
    // as long as you can't find that vector
    // transpose the vectors in the graph
    Island graph = firstVectorGraph;
    do {
      log.add(String.format("transposed g %s", graph));
      node.addTransposed(graph);
      sel.removeIslandFromPossibilities(graph);
      graph = sel.getFirstVectorFromGraph();
    } while (graph != firstVectorWitness);
    node.setLastSelected(firstVectorWitness);
    sel.selectIsland(firstVectorWitness);
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
