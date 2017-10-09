package eu.interedition.collatex.dekker.astar;

import java.util.*;


/*
 * Implementation of the a* algorithm to find the optimal
 * solution in a decision tree.
 *
 * @author: Ronald Haentjens Dekker
 */
public abstract class AstarAlgorithm<N, C extends Cost<C>> {
  // The map of navigated nodes.
  protected Map<N, N> cameFrom;

  protected List<N> aStar(N startNode, C startCost) {
    // The set of nodes already evaluated.
    Set<N> closed = new HashSet<>();
    cameFrom = new HashMap<>();

    // Cost from start along best known path.
    Map<N, C> gScore = new HashMap<>();
    gScore.put(startNode, startCost);

    // Estimated total cost from start to goal through y.
    final Map<N, C> fScore = new HashMap<>();
    fScore.put(startNode, gScore.get(startNode).plus(heuristicCostEstimate(startNode)));

    // The set of tentative nodes to be evaluated, initially containing the start node
    Comparator<N> comp = Comparator.comparing(fScore::get);
    PriorityQueue<N> open = new PriorityQueue<>(10, comp);
    open.add(startNode);

    while(!open.isEmpty()) {
      N current = open.poll();
      if (isGoal(current)) {
        return reconstructPath(cameFrom, current);
      }
      closed.add(current);
      for (N neighbor : neighborNodes(current)) {
        if (closed.contains(neighbor)) {
          continue;
        }
        C tentativeGScore = gScore.get(current).plus(distBetween(current, neighbor));
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

  protected List<N> reconstructPath(Map<N, N> cameFrom, N current) {
    ArrayList<N> path = new ArrayList<>();
    do {
      path.add(0, current);
      current = cameFrom.get(current);
    } while (current != null);
    return path;
  }

  protected abstract boolean isGoal(N node);

  protected abstract Iterable<N> neighborNodes(N current);

  protected abstract C heuristicCostEstimate(N node);

  protected abstract C distBetween(N current, N neighbor);

}
