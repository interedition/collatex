/*
 * Copyright (c) 2013 The Interedition Development Group.
 *
 * This file is part of CollateX.
 *
 * CollateX is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CollateX is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CollateX.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.interedition.collatex.medite;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.SortedSet;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class AlignmentDecisionGraph {

  private final List<Phrase<Match.WithTokenIndex>> matches;
  private final Function<Phrase<Match.WithTokenIndex>, Integer> matchEvaluator;
  private final PriorityQueue<Node> bestPaths;
  private final Map<Node, Integer> minCosts;

  AlignmentDecisionGraph(List<Phrase<Match.WithTokenIndex>> matches, Function<Phrase<Match.WithTokenIndex>, Integer> matchEvaluator) {
    this.matches = matches;
    this.matchEvaluator = matchEvaluator;
    this.bestPaths = new PriorityQueue<Node>(matches.size(), PATH_COST_COMPARATOR);
    this.minCosts = Maps.newHashMap();
  }

  static SortedSet<Phrase<Match.WithTokenIndex>> filter(SortedSet<Phrase<Match.WithTokenIndex>> matches, Function<Phrase<Match.WithTokenIndex>, Integer> matchEvaluator) {
    final SortedSet<Phrase<Match.WithTokenIndex>> alignments = Sets.newTreeSet();

    final List<Phrase<Match.WithTokenIndex>> matchList = Lists.newArrayList(matches);
    Node optimal = new AlignmentDecisionGraph(matchList, matchEvaluator).findBestPath();
    while (optimal.matchIndex >= 0) {
      if (optimal.aligned) {
        alignments.add(matchList.get(optimal.matchIndex));
      }
      optimal = optimal.previous;
    }
    return alignments;
  }

  private Node findBestPath() {
    bestPaths.add(new Node(-1, false));
    while (!bestPaths.isEmpty()) {
      final Node current = bestPaths.remove();
      if (current.matchIndex == matches.size() - 1) {
        return current;
      }
      for (Node successor : current.successors()) {
        final int tentativeCost = cost(current) + cost(successor);
        if (bestPaths.contains(successor) && tentativeCost >= minCosts.get(successor)) {
          continue;
        }
        minCosts.put(successor, tentativeCost);

        successor.cost = tentativeCost + heuristicCost(successor);
        successor.previous = current;
        bestPaths.remove(successor);
        bestPaths.add(successor);
      }
    }
    throw new IllegalStateException("No optimal alignment found");
  }

  private int heuristicCost(Node path) {
    final Phrase<Match.WithTokenIndex> evaluated = matches.get(path.matchIndex);
    final Match.WithTokenIndex lastMatch = evaluated.last();

    int cost = 0;
    for (Phrase<Match.WithTokenIndex> following : matches.subList(path.matchIndex + 1, matches.size())) {
      final Match.WithTokenIndex followingFirstMatch = following.first();
      if (lastMatch.vertexRank < followingFirstMatch.vertexRank && lastMatch.token < followingFirstMatch.token) {
        // we still can align this following match as the matched components are to the right of this path's last match
        continue;
      }
      // we cannot align this following match, so add it to the cost
      cost += value(following);
    }
    return cost;
  }

  private int cost(Node current) {
    int cost = 0;
    while (current != null && current.matchIndex >= 0) {
      if (!current.aligned) {
        cost += value(matches.get(current.matchIndex));
      }
      current = current.previous;
    }
    return cost;
  }

  private int value(Phrase<Match.WithTokenIndex> match) {
    return matchEvaluator.apply(match);
  }

  static class Node {
    final int matchIndex;
    final boolean aligned;
    Node previous;
    int cost;

    Node(int matchIndex, boolean aligned) {
      this.matchIndex = matchIndex;
      this.aligned = aligned;
    }

    Node[] successors() {
      final int nextIndex = matchIndex + 1;
      return new Node[] { new Node(nextIndex, true), new Node(nextIndex, false) };
    }

    @Override
    public boolean equals(Object obj) {
      if (obj != null && obj instanceof Node) {
        final Node other = (Node) obj;
        return (matchIndex == other.matchIndex) && (aligned == other.aligned);
      }
      return super.equals(obj);
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(matchIndex, aligned);
    }
  }

  static final Comparator<Node> PATH_COST_COMPARATOR = new Comparator<Node>() {
    @Override
    public int compare(Node o1, Node o2) {
      return (o1.cost - o2.cost);
    }
  };
}
