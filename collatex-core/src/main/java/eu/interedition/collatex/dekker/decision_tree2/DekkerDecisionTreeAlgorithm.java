package eu.interedition.collatex.dekker.decision_tree2;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.Sets;
import com.google.common.collect.TreeRangeSet;

import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.dekker.astar.AstarAlgorithm;
import eu.interedition.collatex.dekker.matrix.Coordinate;
import eu.interedition.collatex.dekker.matrix.Island;
import eu.interedition.collatex.dekker.matrix.MatchTable;
import eu.interedition.collatex.jung.JungVariantGraph;
import eu.interedition.collatex.simple.SimpleToken;
import eu.interedition.collatex.simple.SimpleWitness;

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
  private MatchTable table;
  protected final static Logger LOG = Logger.getLogger(DekkerDecisionTreeAlgorithm.class.getName());
  
  public DekkerDecisionTreeAlgorithm() {
    this.selection = Maps.newHashMap();
    this.log = Lists.newArrayList();
  }
  
  @Override
  public void collate(VariantGraph against, Iterable<Token> witness) {
    //create root node
    DecisionTreeNode root = createRootNode(against, witness);
    
    //aligning
    LOG.fine("Aligning...");
    List<DecisionTreeNode> nodes = aStar(root, new AlignmentCost());
    //debugAlignment(nodes);
    Set<Island> alignedIslands = Sets.newHashSet();
    for (DecisionTreeNode n : nodes) {
      if (n.getLastSelected()!=null) {
        alignedIslands.add(n.getLastSelected());
      }
    }
    
    //merging
    LOG.fine("Merging...");
    MatchTable table = selection.get(root).getTable();
    Map<Token, VariantGraph.Vertex> map = Maps.newHashMap();
    for (Island island : alignedIslands) {
      for (Coordinate c : island) {
        map.put(table.tokenAt(c.getRow(), c.getColumn()), table.vertexAt(c.getRow(), c.getColumn()));
      }
    }
    merge(against, witness, map);
    LOG.fine("DONE");
  }

  
  // convenience method for testing
  DecisionTreeNode createRoot(SimpleWitness a, SimpleWitness b) {
    final VariantGraph graph = new JungVariantGraph();
    VariantGraphBuilder.addFirstWitnessToGraph(graph, a);
    return createRootNode(graph, b);
  }

  private DecisionTreeNode createRootNode(VariantGraph against, Iterable<Token> witness) {
    // temporary code to add the first witness to the graph
    // without having to actually align them.
    if (against.witnesses().isEmpty()) {
      VariantGraphBuilder.addFirstWitnessToGraph(against, witness);
    }
    LOG.fine("Building MatchTable");
    table = MatchTable.create(against, witness);
    ExtendedMatchTableSelection selection = new ExtendedMatchTableSelection(table);
    DecisionTreeNode root = new DecisionTreeNode();
    associate(root, selection);
    return root;
  }

  
  void associate(DecisionTreeNode node, ExtendedMatchTableSelection _selection) {
    selection.put(node, _selection);
  }

  @Override
  protected boolean isGoal(DecisionTreeNode node) {
    boolean finished = selection.get(node).isFinished();
    //if (finished) {
      //debugAlignmentSoFar(node);
    //}
    return finished;
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
    if (LOG.isLoggable(Level.FINER)) {
      debugPath(current);
      selection.get(current).debugPossibleVectors();
    }
    //TODO: change next line; should be no neighbors
    //TODO: to test
    if (selection.get(current).isFinished()) {
      throw new RuntimeException("THIS SHOULD NOT HAPPEN!");
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
//      DecisionTreeNode addNode3 = addNode(childNodes, current);
//      skipFirstVectorFromGraph(addNode3, firstVectorGraph);
//      DecisionTreeNode addNode4 = addNode(childNodes, current);
//      skipFirstVectorFromWitness(addNode4, firstVectorWitness);
    } else {
      // make 2 copies
      DecisionTreeNode addNode = addNode(childNodes, current);
      selectFirstVectorFromGraph(addNode, firstVectorGraph);
      DecisionTreeNode addNode2 = addNode(childNodes, current);
      skipFirstVectorFromGraph(addNode2, firstVectorGraph);
    }
    if (LOG.isLoggable(Level.FINER)) {
      debugCostChildren(current, childNodes);
    }
    return childNodes;
  } 

  private void debugPath(DecisionTreeNode current) {
    List<DecisionTreeNode> path = reconstructPath(cameFrom, current);
    debugPath(path);
  }

  private void debugPath(List<DecisionTreeNode> path) {
    StringBuilder builder = new StringBuilder();
    builder.append(">>");
    Island previous = null;
    for (DecisionTreeNode node : path) {
      if (previous!=null) {
        builder.append("{gap}");
      }
      if (previous!=node.getLastSelected()) {
        Island last = node.getLastSelected();
        String tokenString = islandToTokens(last);
        builder.append(tokenString);
      }
      previous = node.getLastSelected();
    }
    builder.append("<<");
    LOG.finer(builder.toString());
  }

  private String islandToTokens(Island last) {
    List<Token> witnessTokens = Lists.newArrayList();
    for (Coordinate c : last) {
      witnessTokens.add(table.tokenAt(c.getRow(), c.getColumn()));
    }
    String tokenString = SimpleToken.toString(witnessTokens);
    return tokenString;
  }

  private void debugCostChildren(DecisionTreeNode current, Set<DecisionTreeNode> childNodes) {
    // debug result
    for (DecisionTreeNode node : childNodes) {
      AlignmentCost cost = current == null ? new AlignmentCost() : distBetween(current, node);
      AlignmentCost heuristic = heuristicCostEstimate(node);
      LOG.finer("additional "+cost+"; heurestic: "+heuristic);
    }
  }

  private DecisionTreeNode addNode(Set<DecisionTreeNode> childNodes, DecisionTreeNode current) {
    DecisionTreeNode child = new DecisionTreeNode();
    ExtendedMatchTableSelection childSelection = selection.get(current).copy();
    selection.put(child, childSelection);
    child.setLastSelected(current.getLastSelected());
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
  protected AlignmentCost distBetween(DecisionTreeNode current, DecisionTreeNode neighbor) {
    Island prev = current.getLastSelected();
    Island next = neighbor.getLastSelected();
    // four cases
    // 1) we are at the start node
    // 2) we are in the middle
    // 3) we are at the end node
    // 4) no vector is chosen to be aligned, one vector is skipped instead
    // we should also check whether there are possibilities left
    // a vector is skipped
    AlignmentCost base=null;
    if (prev==next) {
      //no new vector is selected... 
      //but some vector has been skipped!
      //depending on the potential vectors that are still selectable
      // a penalty in the future cost will occur
      base = new AlignmentCost(0, 0);
    }
    // a vector is selected to be aligned
    // and the previous node is not the start node
    else if (prev!=null) {
      int distanceGraph = next.getLeftEnd().getColumn() - prev.getRightEnd().getColumn() - 1;
      int distanceWitness = next.getLeftEnd().getRow() - prev.getRightEnd().getRow() - 1;
      int gaps = Math.max(distanceGraph, distanceWitness);
      base = new AlignmentCost(1, gaps);
    }
    // a vector is selected to be aligned
    // and the previous node is the start node
    else if (prev==null) {
      int distanceGraph = next.getLeftEnd().getColumn();
      int distanceWitness = next.getLeftEnd().getRow();
      int gaps = Math.max(distanceGraph, distanceWitness);
      base = new AlignmentCost(1, gaps);
    }
    // if at the end of the decision tree
    // calculate remaining gap (max graph/witness).
    if (!selection.get(neighbor).isFinished()) {
      return base;
    }
    // calculate distance (gaps) from neighbor to end
    //TODO: CHECK CHECK!
    if (next==null) {
      int gapGraph = selection.get(neighbor).sizeOfGraph();
      int gapWitness = selection.get(neighbor).sizeOfWitness();
      int gaps = Math.max(gapGraph, gapWitness);
      AlignmentCost gap = new AlignmentCost(0, gaps);
      return base.plus(gap);
    }
    int gapGraph = selection.get(neighbor).sizeOfGraph() - next.getRightEnd().getColumn() -1;
    int gapWitness = selection.get(neighbor).sizeOfWitness() - next.getRightEnd().getRow() -1;
    int gaps = Math.max(gapGraph, gapWitness);
    AlignmentCost gap = new AlignmentCost(0, gaps);
    return base.plus(gap);
  }

  @Override
  protected AlignmentCost heuristicCostEstimate(DecisionTreeNode node) {
    // some vectors can overlap
    // convert the vectors that are still possible into ranges
    // put all the ranges in one set
    // then calculate gaps
    // two ways to do this.. 
    // 1) get total - aligned tokens - surface of possible vectors
    // 2) find gaps between last select vector and possible vectors
    // second way is implemented here
    ExtendedMatchTableSelection slc = selection.get(node);
    List<Island> islands = slc.getPossibleIslands();
    
    //There are two dimensions: ranges in graph or witness
    int gapsGraph = calculateFutureGapsGraph(node, islands);
    int gapsWitness = calculateFutureGapsWitness(node, islands);
    return new AlignmentCost(0, Math.max(gapsGraph, gapsWitness));
  }

  private int calculateFutureGapsGraph(DecisionTreeNode node, List<Island> islands) {
    RangeSet<Integer> s = TreeRangeSet.create();
    for (Island i : islands) {
      Range<Integer> r = Range.closed(i.getLeftEnd().getColumn(), i.getRightEnd().getColumn());
      s.add(r);
    }
    Island last = node.getLastSelected();
    int end = last==null ? -1 : last.getRightEnd().getColumn();
    return calculateGapsForAGivenRangeSet(s, end);
  }

  private int calculateFutureGapsWitness(DecisionTreeNode node, List<Island> islands) {
    RangeSet<Integer> s = TreeRangeSet.create();
    for (Island i : islands) {
      Range<Integer> r = Range.closed(i.getLeftEnd().getRow(), i.getRightEnd().getRow());
      s.add(r);
    }
    Island last = node.getLastSelected();
    int end = last==null ? -1 : last.getRightEnd().getRow();
    return calculateGapsForAGivenRangeSet(s, end);
  }

  private int calculateGapsForAGivenRangeSet(RangeSet<Integer> s, int end) {
    int gaps = 0;
    for (Range<Integer> r : s.asRanges()) {
      int gap = r.lowerEndpoint() - end -1;
      gaps += gap;
      end = r.upperEndpoint();
    }
    return gaps;
  }

}
