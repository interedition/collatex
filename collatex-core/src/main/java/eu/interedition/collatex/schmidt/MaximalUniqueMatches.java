package eu.interedition.collatex.schmidt;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Ordering;
import com.google.common.collect.Range;
import com.google.common.collect.Ranges;
import com.google.common.collect.Sets;
import com.google.common.collect.SortedSetMultimap;
import com.google.common.collect.TreeMultimap;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.util.VariantGraphRanking;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class MaximalUniqueMatches extends HashMap<List<VariantGraph.Vertex>,List<Integer>> {
  private static final Logger LOG = Logger.getLogger(MaximalUniqueMatches.class.getName());

  public static SortedMap<List<VariantGraph.Vertex>,List<Token>> find(Comparator<Token> comparator, VariantGraph graph, Iterable<Token> tokens) {
    Stopwatch stopwatch = null;
    final boolean logTimings = LOG.isLoggable(Level.FINER);

    if (logTimings) {
      stopwatch = new Stopwatch();
      stopwatch.start();
    }

    final SuffixTree<Token> suffixTree = SuffixTree.build(comparator, Iterables.toArray(tokens, Token.class));

    if (logTimings) {
      stopwatch.stop();
      LOG.log(Level.FINER, "Built suffix tree of {0} in {1}", new Object[] { tokens, stopwatch });
      stopwatch = new Stopwatch();
      stopwatch.start();
    }

    final VariantGraphRanking ranking = VariantGraphRanking.of(graph);

    if (logTimings) {
      stopwatch.stop();
      LOG.log(Level.FINER, "Ranked variant graph of {0} in {1}", new Object[] { graph, stopwatch });
      stopwatch = new Stopwatch();
      stopwatch.start();
    }

    final SortedSetMultimap<Integer,VariantGraph.Vertex> rankMap = ranking.getByRank();
    final Multimap<Integer, MatchThread> matchThreads = HashMultimap.create();
    for (Integer rank : rankMap.keySet()) {
      final SortedSet<VariantGraph.Vertex> vertices = rankMap.get(rank);

      for (VariantGraph.Vertex vertex : vertices) {
        final MatchThread matchThread = new MatchThread(suffixTree).advance(vertex);
        if (matchThread != null) {
          matchThreads.put(rank, matchThread);
        }
      }
      for (MatchThread matchThread : matchThreads.get(rank - 1)) {
        for (VariantGraph.Vertex vertex : vertices) {
          final MatchThread advanced = matchThread.advance(vertex);
          if (advanced != null) {
            matchThreads.put(rank, advanced);
            break;
          }
        }
      }
    }

    if (logTimings) {
      stopwatch.stop();
      LOG.log(Level.FINER, "Found {0} match phrase(s) in {1}", new Object[] { matchThreads.size(), stopwatch });
      stopwatch = new Stopwatch();
      stopwatch.start();
    }

    final Set<Range<Integer>> excludedRanks = Sets.newHashSet();
    final Set<Range<Integer>> excludedTokens = Sets.newHashSet();
    final Function<List<Match>, Map<List<VariantGraph.Vertex>,List<Integer>>> availableMatch = new Function<List<Match>, Map<List<VariantGraph.Vertex>, List<Integer>>>() {
      @Override
      public Map<List<VariantGraph.Vertex>, List<Integer>> apply(@Nullable List<Match> input) {
        final List<VariantGraph.Vertex> vertices = Lists.newArrayListWithExpectedSize(input.size());
        final LinkedList<Integer> tokens = Lists.newLinkedList();

        for (Match match : input) {
          final Integer vertexRank = ranking.apply(match.vertex);
          for (Range<Integer> excludedRange : excludedRanks) {
            if (excludedRange.contains(vertexRank)) {
              return null;
            }
          }
          final Integer last = tokens.peek();
          boolean matchFound = false;
          for (int mc = 0; mc < match.equivalenceClass.length; mc++) {
            if (last != null && (last != match.equivalenceClass.members[mc] + 1)) {
              continue;
            }
            boolean notExcluded = true;
            for (Range<Integer> excludedRange : excludedTokens) {
              if (excludedRange.contains(match.equivalenceClass.members[mc])) {
                notExcluded = false;
                break;
              }
            }
            if (notExcluded) {
              matchFound = true;
              vertices.add(0, match.vertex);
              tokens.add(0, match.equivalenceClass.members[mc]);
              break;
            }
          }
          if (!matchFound) {
            return null;
          }
        }

        return Collections.<List<VariantGraph.Vertex>, List<Integer>>singletonMap(vertices, tokens);
      }
    };

    final Multimap<Integer,List<Match>> matchesByLength = TreeMultimap.create(Ordering.natural().reverse(), new Comparator<List<Match>>() {
      @Override
      public int compare(List<Match> o1, List<Match> o2) {
        // compare by match length
        int result = o2.size() - o1.size();
        if (result != 0) {
          return result;
        }

        // compare by first token
        result = o2.get(0).equivalenceClass.compareTo(o1.get(0).equivalenceClass);
        if (result != 0) {
          return result;
        }

        // compare by first vertex ranking
        return (ranking.apply(o2.get(0).vertex) - ranking.apply(o1.get(0).vertex));
      }
    });

    for (List<Match> match : Iterables.transform(matchThreads.values(), toMatchList())) {
      matchesByLength.put(match.size(), match);
    }

    if (logTimings) {
      stopwatch.stop();
      LOG.log(Level.FINER, "Sorted {0} match phrase(s) in {1}", new Object[] { matchThreads.size(), stopwatch });
      stopwatch = new Stopwatch();
      stopwatch.start();
    }

    final SortedMap<List<VariantGraph.Vertex>, List<Token>> mums = Maps.newTreeMap(new Comparator<List<VariantGraph.Vertex>>() {
      @Override
      public int compare(List<VariantGraph.Vertex> o1, List<VariantGraph.Vertex> o2) {
        return (ranking.apply(o1.get(0)) - ranking.apply(o2.get(0)));
      }
    });

    while (true) {
      Map<List<VariantGraph.Vertex>, List<Integer>> maximalUniqueMatch = null;
      final Set<List<Match>> exhaustedMatches = Sets.newHashSet();

      for (Integer matchLength : matchesByLength.keySet()) {
        final Collection<List<Match>> matchesOfSameLength = matchesByLength.get(matchLength);

        if (matchesOfSameLength.size() == 1) {
          final List<Match> onlyMatch = Iterables.get(matchesOfSameLength, 0);
          maximalUniqueMatch = availableMatch.apply(onlyMatch);
          if (maximalUniqueMatch != null) {
            break;
          } else {
            exhaustedMatches.add(onlyMatch);
          }
        }
        if (maximalUniqueMatch == null) {
          List<Match> prev = null;
          for (List<Match> next : matchesOfSameLength) {
            if (prev != null && prev.get(0).equivalenceClass.equals(next.get(0).equivalenceClass)) {
              maximalUniqueMatch = availableMatch.apply(prev);
              if (maximalUniqueMatch != null) {
                break;
              }
              exhaustedMatches.add(prev);
              prev = next;
            }
          }
        }
        if (maximalUniqueMatch != null) {
          break;
        }
      }

      if (maximalUniqueMatch == null) {
        for (Integer matchLength : matchesByLength.keySet()) {
          for (List<Match> match : matchesByLength.get(matchLength)) {
            maximalUniqueMatch = availableMatch.apply(match);
            if (maximalUniqueMatch != null) {
              break;
            }
            exhaustedMatches.add(match);
          }
          if (maximalUniqueMatch != null) {
            break;
          }
        }
      }
      if (maximalUniqueMatch == null) {
        break;
      }

      matchesByLength.values().removeAll(exhaustedMatches);

      final Map.Entry<List<VariantGraph.Vertex>, List<Integer>> matchPhrases = Iterables.get(maximalUniqueMatch.entrySet(), 0);
      final List<VariantGraph.Vertex> vertexPhrase = matchPhrases.getKey();
      final List<Integer> tokenIndexPhrase = matchPhrases.getValue();

      excludedRanks.add(Ranges.closed(ranking.apply(vertexPhrase.get(0)), ranking.apply(vertexPhrase.get(vertexPhrase.size() - 1))));
      excludedTokens.add(Ranges.closed(tokenIndexPhrase.get(0), tokenIndexPhrase.get(tokenIndexPhrase.size() - 1)));

      final List<Token> tokenPhrase = Lists.newArrayListWithExpectedSize(tokenIndexPhrase.size());
      for (Integer tokenIndex : tokenIndexPhrase) {
        tokenPhrase.add(suffixTree.source[tokenIndex]);
      }
      Preconditions.checkState(mums.put(vertexPhrase, tokenPhrase) == null, "Duplicate MUM");
    }

    if (logTimings) {
      stopwatch.stop();
      LOG.log(Level.FINER, "Found {0} maximum unique matche(s) out of {1} match phrase(s) in {2}", new Object[]{mums.size(), matchThreads.size(), stopwatch});
    }

    return mums;
  }

  /**
   * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
   */
  static class MatchThread {

    final MatchThread previous;
    final SuffixTree<Token>.Cursor cursor;
    final VariantGraph.Vertex vertex;

    MatchThread(SuffixTree<Token> suffixTree) {
      this(null, suffixTree.cursor(), null);
    }

    MatchThread(MatchThread previous, SuffixTree<Token>.Cursor cursor, VariantGraph.Vertex vertex) {
      this.previous = previous;
      this.cursor = cursor;
      this.vertex = vertex;
    }

    MatchThread advance(VariantGraph.Vertex vertex) {
      final Set<Token> tokens = vertex.tokens();
      if (!tokens.isEmpty()) {
        final SuffixTree<Token>.Cursor next = cursor.move(Iterables.get(tokens, 0));
        if (next != null) {
          return new MatchThread(this, next, vertex);
        }
      }
      return null;
    }
  }

  /**
   * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
   */
  static class Match {
    final VariantGraph.Vertex vertex;
    final SuffixTree.EquivalenceClass equivalenceClass;

    Match(VariantGraph.Vertex vertex, SuffixTree.EquivalenceClass equivalenceClass) {
      this.vertex = vertex;
      this.equivalenceClass = equivalenceClass;
    }

    @Override
    public String toString() {
      return "{" + vertex + " -> " + equivalenceClass + "}";
    }
  }

  static Function<MatchThread,List<Match>> toMatchList() {
    return new Function<MatchThread, List<Match>>() {
      @Override
      public List<Match> apply(@Nullable MatchThread input) {
        final List<Match> matches = Lists.newLinkedList();

        MatchThread current = input;
        while (current.vertex != null) {
          matches.add(new Match(current.vertex, current.cursor.matchedClass()));
          current = current.previous;
        }

        return matches;
      }
    };
  }
}
