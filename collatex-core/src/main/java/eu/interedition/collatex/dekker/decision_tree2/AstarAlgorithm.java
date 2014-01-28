package eu.interedition.collatex.dekker.decision_tree2;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import eu.interedition.collatex.CollationAlgorithm;


/*
 * Implementation of the a* algorithm to find the optimal
 * solution in a decision tree.
 * 
 * @author: Ronald Haentjens Dekker
 */
public abstract class AstarAlgorithm<T extends Cost<T>> extends CollationAlgorithm.Base {

  public List<DecisionTreeNode> aStar(DecisionTreeNode start, T startCost) {
    // The set of nodes already evaluated.
    Set<DecisionTreeNode> closed = Sets.newHashSet();
    // The map of navigated nodes.
    Map<DecisionTreeNode, DecisionTreeNode> cameFrom = Maps.newHashMap();
    
    // Cost from start along best known path.
    Map<DecisionTreeNode, T> gScore = Maps.newHashMap();
    gScore.put(start, startCost);
  
    // Estimated total cost from start to goal through y.
    final Map<DecisionTreeNode, T> fScore = Maps.newHashMap();
    fScore.put(start, gScore.get(start).plus(heuristicCostEstimate(start)));
    
    // The set of tentative nodes to be evaluated, initially containing the start node
    Comparator<DecisionTreeNode> comp = new Comparator<DecisionTreeNode>() {
      @Override
      public int compare(DecisionTreeNode arg0, DecisionTreeNode arg1) {
        return fScore.get(arg0).compareTo(fScore.get(arg1));
      }
    };
    PriorityQueue<DecisionTreeNode> open = new PriorityQueue<DecisionTreeNode>(10, comp);
    open.add(start);

    while(!open.isEmpty()) {
      DecisionTreeNode current = open.poll();
      if (isGoal(current)) {
        return reconstructPath(cameFrom, current);
      }
      closed.add(current);
      for (DecisionTreeNode neighbor : neighborNodes(current)) {
        if (closed.contains(neighbor)) {
          continue;
        }
        T tentativeGScore = gScore.get(current).plus(distBetween(current, neighbor));
        if (!open.contains(neighbor)||tentativeGScore.compareTo(gScore.get(neighbor))<0) {
          cameFrom.put(neighbor, current);
          gScore.put(neighbor, tentativeGScore);
          fScore.put(neighbor, gScore.get(neighbor).plus(heuristicCostEstimate(neighbor)));
          if (!open.contains(neighbor)) {
            open.add(neighbor);
          }
        }
      }
    }
    throw new IllegalStateException("No node found that suits goal condition!");
  }

  private List<DecisionTreeNode> reconstructPath(Map<DecisionTreeNode, DecisionTreeNode> cameFrom, DecisionTreeNode current) {
    ArrayList<DecisionTreeNode> path = Lists.newArrayList();
    do {
      path.add(0, current);
      current = cameFrom.get(current);
    } while (current != null);
    return path;
  }

  abstract boolean isGoal(DecisionTreeNode node);

  abstract Iterable<DecisionTreeNode> neighborNodes(DecisionTreeNode current);

  abstract T heuristicCostEstimate(DecisionTreeNode node);

  abstract T distBetween(DecisionTreeNode current, DecisionTreeNode neighbor);

}
