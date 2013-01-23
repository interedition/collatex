package eu.interedition.collatex.medite;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.collect.Ranges;
import com.google.common.collect.Sets;
import com.google.common.collect.SortedSetMultimap;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.util.VariantGraphRanking;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;
import java.util.SortedSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class Matches extends ArrayList<Phrase<Match.WithEquivalence>> {
  private static final Logger LOG = Logger.getLogger(Matches.class.getName());

  public Matches(int initialCapacity) {
    super(initialCapacity);
  }

  public static Matches between(Comparator<Token> comparator, VariantGraphRanking ranking, SuffixTree<Token> suffixTree) {

    final SortedSetMultimap<Integer,VariantGraph.Vertex> rankMap = ranking.getByRank();
    final Multimap<Integer, MatchThread> matchThreads = HashMultimap.create();
    for (Integer rank : rankMap.keySet()) {
      final SortedSet<VariantGraph.Vertex> vertices = rankMap.get(rank);

      for (VariantGraph.Vertex vertex : vertices) {
        final MatchThread matchThread = new MatchThread(suffixTree).advance(vertex, rank);
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

    final Matches matcher = new Matches(matchThreads.size());
    for (MatchThread matchThread : matchThreads.values()) {
      final Phrase<Match.WithEquivalence> phrase = new Phrase<Match.WithEquivalence>();

      MatchThread current = matchThread;
      while (current.vertex != null) {
        phrase.add(new Match.WithEquivalence(current.vertex, current.vertexRank, current.cursor.matchedClass()));
        current = current.previous;
      }

      matcher.add(phrase);
    }
    Collections.sort(matcher, MAXIMUM_UNIQUE_MATCH_ORDERING);

    return matcher;
  }

  public SortedSet<Phrase<Match.WithTokenIndex>> removeMaximalUniqueMatches(IndexRangeSet rankFilter, IndexRangeSet tokenFilter) {
    rankFilter = new IndexRangeSet(rankFilter);
    tokenFilter = new IndexRangeSet(tokenFilter);

    final Function<Phrase<Match.WithEquivalence>, Phrase<Match.WithTokenIndex>> tokenSelector = tokenSelector(rankFilter, tokenFilter);
    final SortedSet<Phrase<Match.WithTokenIndex>> maximalUniqueMatches = Sets.newTreeSet();

    while (true) {
      final Set<Phrase<Match.WithEquivalence>> exhaustedMatches = Sets.newHashSet();
      Phrase<Match.WithTokenIndex> nextMum = null;

      final int numPhrases = size();
      for (int pc = 0; pc < (numPhrases - 1); pc++) {
        final Phrase<Match.WithEquivalence> candidate = get(pc);
        final Phrase<Match.WithEquivalence> successor = get(pc + 1);

        if (candidate.size() > successor.size() || candidate.first().equivalenceClass.equals(successor.first().equivalenceClass)) {
          nextMum = tokenSelector.apply(candidate);
          if (nextMum != null) {
            break;
          } else {
            exhaustedMatches.add(candidate);
          }
        }
      }
      if (nextMum == null) {
        for (Phrase<Match.WithEquivalence> candidate : this) {
          nextMum = tokenSelector.apply(candidate);
          if (nextMum != null) {
            break;
          }
          exhaustedMatches.add(candidate);
        }
      }
      if (nextMum == null) {
        break;
      }

      Preconditions.checkState(maximalUniqueMatches.add(nextMum), "Duplicate MUM");

      removeAll(exhaustedMatches);

      rankFilter.add(Ranges.closed(nextMum.first().vertexRank, nextMum.last().vertexRank));
      tokenFilter.add(Ranges.closed(nextMum.first().token, nextMum.last().token));
    }
    return maximalUniqueMatches;
  }

  static Function<Phrase<Match.WithEquivalence>, Phrase<Match.WithTokenIndex>> tokenSelector(final IndexRangeSet rankFilter, final IndexRangeSet tokenFilter) {
    return new Function<Phrase<Match.WithEquivalence>, Phrase<Match.WithTokenIndex>>() {
      @Override
      public Phrase<Match.WithTokenIndex> apply(@Nullable Phrase<Match.WithEquivalence> input) {
        final Phrase<Match.WithTokenIndex> tokenPhrase = new Phrase<Match.WithTokenIndex>();
        Integer lastToken = null;
        for (Match.WithEquivalence match : input) {
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
              tokenPhrase.add(new Match.WithTokenIndex(match.vertex, match.vertexRank, lastToken = tokenCandidate));
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

  static final Comparator<Phrase<Match.WithEquivalence>> MAXIMUM_UNIQUE_MATCH_ORDERING = new Comparator<Phrase<Match.WithEquivalence>>() {
    @Override
    public int compare(Phrase<Match.WithEquivalence> o1, Phrase<Match.WithEquivalence> o2) {
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
      return (o2.first().vertexRank - o1.first().vertexRank);
    }
  };

}
