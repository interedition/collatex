package eu.interedition.collatex.medite;

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
public class Aligner {

  private final List<Phrase<Match.WithTokenIndex>> matches;
  private final PriorityQueue<Path> bestPaths;
  private final Map<Path, Integer> minCosts;

  Aligner(List<Phrase<Match.WithTokenIndex>> matches) {
    this.matches = matches;
    this.bestPaths = new PriorityQueue<Path>(matches.size(), PATH_COST_COMPARATOR);
    this.minCosts = Maps.newHashMap();
  }

  static SortedSet<Phrase<Match.WithTokenIndex>> align(SortedSet<Phrase<Match.WithTokenIndex>> matches) {
    final SortedSet<Phrase<Match.WithTokenIndex>> alignments = Sets.newTreeSet();

    final List<Phrase<Match.WithTokenIndex>> matchList = Lists.newArrayList(matches);
    Path optimal = new Aligner(matchList).optimize();
    while (optimal.matchIndex >= 0) {
      if (optimal.aligned) {
        alignments.add(matchList.get(optimal.matchIndex));
      }
      optimal = optimal.previous;
    }
    return alignments;
  }

  private Path optimize() {
    bestPaths.add(new Path(null, -1, false, 0));
    while (!bestPaths.isEmpty()) {
      final Path current = bestPaths.remove();
      if (current.matchIndex == matches.size() - 1) {
        return current;
      }
      for (Path successor : current.successors()) {
        final int tentativeCost = cost(successor);
        if (bestPaths.contains(successor) && tentativeCost >= minCosts.get(successor)) {
          continue;
        }
        minCosts.put(successor, tentativeCost);
        bestPaths.remove(successor);
        bestPaths.add(new Path(successor, tentativeCost + heuristicCost(successor)));
      }
    }
    throw new IllegalStateException("No optimal alignment found");
  }

  private int heuristicCost(Path path) {
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
      cost += following.size();
    }
    return cost;
  }

  private int cost(Path current) {
    int cost = 0;
    while (current.matchIndex >= 0) {
      if (!current.aligned) {
        cost += matches.get(current.matchIndex).size();
      }
      current = current.previous;
    }
    return cost;
  }

  static class Path {
    final Path previous;
    final int matchIndex;
    final boolean aligned;
    final int cost;

    Path(Path previous, int matchIndex, boolean aligned, int cost) {
      this.previous = previous;
      this.matchIndex = matchIndex;
      this.aligned = aligned;
      this.cost = cost;
    }

    public Path(Path other, int cost) {
      this(other.previous, other.matchIndex, other.aligned, cost);
    }

    Path[] successors() {
      final int nextIndex = matchIndex + 1;
      return new Path[] { new Path(this, nextIndex, true, cost), new Path(this, nextIndex, false, cost) };
    }

    @Override
    public boolean equals(Object obj) {
      if (obj != null && obj instanceof Path) {
        final Path other = (Path) obj;
        return (matchIndex == other.matchIndex) && (aligned == other.aligned);
      }
      return super.equals(obj);
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(matchIndex, aligned);
    }
  }

  static final Comparator<Path> PATH_COST_COMPARATOR = new Comparator<Path>() {
    @Override
    public int compare(Path o1, Path o2) {
      return (o1.cost - o2.cost);
    }
  };
}
