package eu.interedition.collatex.medite;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.collect.Ordering;
import com.google.common.collect.Ranges;
import com.google.common.collect.Sets;
import com.google.common.collect.SortedSetMultimap;
import com.google.common.collect.TreeMultimap;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.util.VariantGraphRanking;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Comparator;
import java.util.Set;
import java.util.SortedSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class Matcher {
  private static final Logger LOG = Logger.getLogger(Matcher.class.getName());

  private SuffixTree<Token> suffixTree;

  private SortedSetMultimap<Integer,Phrase<EquivalenceClassMatch>> phrases;

  public static Matcher create(Comparator<Token> comparator, VariantGraph graph, Token[] tokens) {

    final Matcher matcher = new Matcher();

    Stopwatch stopwatch = null;
    final boolean logTimings = LOG.isLoggable(Level.FINER);

    if (logTimings) {
      stopwatch = new Stopwatch();
      stopwatch.start();
    }

    matcher.suffixTree = SuffixTree.build(comparator, tokens);

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
        final MatchThread matchThread = new MatchThread(matcher.suffixTree).advance(vertex, rank);
        if (matchThread != null) {
          matchThreads.put(rank, matchThread);
        }
      }
      for (MatchThread matchThread : matchThreads.get(rank - 1)) {
        for (VariantGraph.Vertex vertex : vertices) {
          final MatchThread advanced = matchThread.advance(vertex, rank);
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

    matcher.phrases = TreeMultimap.create(Ordering.natural().reverse(), new Comparator<Phrase<EquivalenceClassMatch>>() {
      @Override
      public int compare(Phrase<EquivalenceClassMatch> o1, Phrase<EquivalenceClassMatch> o2) {
        // 1. reverse ordering by match length
        int result = o2.size() - o1.size();
        if (result != 0) {
          return result;
        }

        // 2. reverse ordering by first token
        result = o2.first().equivalenceClass.compareTo(o1.first().equivalenceClass);
        if (result != 0) {
          return result;
        }

        // 3. reverse ordering by first vertex ranking
        return (ranking.apply(o2.first().vertex) - ranking.apply(o1.first().vertex));
      }
    });

    for (MatchThread matchThread : matchThreads.values()) {
      final Phrase<EquivalenceClassMatch> phrase = new Phrase<EquivalenceClassMatch>();

      MatchThread current = matchThread;
      while (current.vertex != null) {
        phrase.add(new EquivalenceClassMatch(current.vertex, current.vertexRank, current.cursor.matchedClass()));
        current = current.previous;
      }

      matcher.phrases.put(phrase.size(), phrase);
    }

    if (logTimings) {
      stopwatch.stop();
      LOG.log(Level.FINER, "Sorted {0} match phrase(s) in {1}", new Object[] { matchThreads.size(), stopwatch });
      stopwatch = new Stopwatch();
      stopwatch.start();
    }

    return matcher;
  }

  public SortedSet<Phrase<TokenMatch>> maximalUniqueMatches(IndexRangeSet rankFilter, IndexRangeSet tokenFilter) {
    rankFilter = new IndexRangeSet(rankFilter);
    tokenFilter = new IndexRangeSet(tokenFilter);

    final Function<Phrase<EquivalenceClassMatch>, Phrase<TokenMatch>> tokenSelector = tokenSelector(rankFilter, tokenFilter);
    final SortedSet<Phrase<TokenMatch>> maximalUniqueMatches = Sets.newTreeSet();

    while (true) {
      final Set<Phrase<EquivalenceClassMatch>> exhaustedMatches = Sets.newHashSet();

      Phrase<TokenMatch> nextMum = null;

      for (Integer matchLength : phrases.keySet()) {
        final Collection<Phrase<EquivalenceClassMatch>> matchesOfSameLength = phrases.get(matchLength);

        if (matchesOfSameLength.size() == 1) {
          final Phrase<EquivalenceClassMatch> onlyMatch = Iterables.get(matchesOfSameLength, 0);
          nextMum = tokenSelector.apply(onlyMatch);
          if (nextMum != null) {
            break;
          } else {
            exhaustedMatches.add(onlyMatch);
          }
        }
        if (nextMum == null) {
          Phrase<EquivalenceClassMatch> prev = null;
          for (Phrase<EquivalenceClassMatch> next : matchesOfSameLength) {
            if (prev != null && prev.first().equivalenceClass.equals(next.first().equivalenceClass)) {
              nextMum = tokenSelector.apply(prev);
              if (nextMum != null) {
                break;
              }
              exhaustedMatches.add(prev);
              prev = next;
            }
          }
        }
        if (nextMum != null) {
          break;
        }
      }

      if (nextMum == null) {
        for (Integer matchLength : phrases.keySet()) {
          for (Phrase<EquivalenceClassMatch> match : phrases.get(matchLength)) {
            nextMum = tokenSelector.apply(match);
            if (nextMum != null) {
              break;
            }
            exhaustedMatches.add(match);
          }
          if (nextMum != null) {
            break;
          }
        }
      }
      if (nextMum == null) {
        break;
      }

      Preconditions.checkState(maximalUniqueMatches.add(nextMum), "Duplicate MUM");

      phrases.values().removeAll(exhaustedMatches);

      rankFilter.add(Ranges.closed(nextMum.first().vertexRank, nextMum.last().vertexRank));
      tokenFilter.add(Ranges.closed(nextMum.first().token, nextMum.last().token));
    }
    return maximalUniqueMatches;
  }

  static Function<Phrase<EquivalenceClassMatch>, Phrase<TokenMatch>> tokenSelector(final IndexRangeSet rankFilter, final IndexRangeSet tokenFilter) {
    return new Function<Phrase<EquivalenceClassMatch>, Phrase<TokenMatch>>() {
      @Override
      public Phrase<TokenMatch> apply(@Nullable Phrase<EquivalenceClassMatch> input) {
        final Phrase<TokenMatch> tokenPhrase = new Phrase<TokenMatch>();
        Integer lastToken = null;
        for (EquivalenceClassMatch match : input) {
          if (rankFilter.apply(match.vertexRank)) {
            return null;
          }

          boolean matchFound = false;
          for (int mc = 0; mc < match.equivalenceClass.length; mc++) {
            final int tokenCandidate = match.equivalenceClass.members[mc];
            if (lastToken != null && (lastToken + 1) != tokenCandidate) {
              continue;
            }
            if (!tokenFilter.apply(tokenCandidate)) {
              matchFound = true;
              tokenPhrase.add(new TokenMatch(match.vertex, match.vertexRank, lastToken = tokenCandidate));
              break;
            }
          }
          if (!matchFound) {
            return null;
          }
        }
        return tokenPhrase;
      }
    };
  }

  /**
   * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
   */
  static class MatchThread {

    final MatchThread previous;
    final VariantGraph.Vertex vertex;
    final int vertexRank;
    final SuffixTree<Token>.Cursor cursor;

    MatchThread(SuffixTree<Token> suffixTree) {
      this(null, null, -1, suffixTree.cursor());
    }

    MatchThread(MatchThread previous, VariantGraph.Vertex vertex, int vertexRank, SuffixTree<Token>.Cursor cursor) {
      this.previous = previous;
      this.vertex = vertex;
      this.vertexRank = vertexRank;
      this.cursor = cursor;
    }

    MatchThread advance(VariantGraph.Vertex vertex, int vertexRank) {
      final Set<Token> tokens = vertex.tokens();
      if (!tokens.isEmpty()) {
        final SuffixTree<Token>.Cursor next = cursor.move(Iterables.get(tokens, 0));
        if (next != null) {
          return new MatchThread(this, vertex, vertexRank, next);
        }
      }
      return null;
    }
  }


}
