package eu.interedition.collatex.schmidt;

import com.google.common.base.Function;
import com.google.common.collect.DiscreteDomains;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Ordering;
import com.google.common.collect.Range;
import com.google.common.collect.Ranges;
import com.google.common.collect.Sets;
import com.google.common.collect.SortedSetMultimap;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.util.VariantGraphRanking;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class MaximalUniqueMatches extends HashMap<List<VariantGraph.Vertex>,List<Integer>> {

  public static MaximalUniqueMatches find(Comparator<Token> comparator, VariantGraph graph, Iterable<Token> tokens) {
    return new MaximalUniqueMatches().find(
            VariantGraphRanking.of(graph), Collections.<Range<Integer>>emptySet(),
            SuffixTree.build(comparator, Iterables.toArray(tokens, Token.class)), Collections.<Range<Integer>>emptySet(),
            null
    );
  }

  MaximalUniqueMatches find(VariantGraphRanking ranking, Set<Range<Integer>> excludedRanks, SuffixTree<Token> suffixTree, Set<Range<Integer>> excludedTokens, List<Match> matchPhrase) {
    if (matchPhrase != null) {
      final List<VariantGraph.Vertex> vertexMatches = Lists.newLinkedList();
      final List<Integer> tokenMatches = Lists.newLinkedList();

      for (Match match : matchPhrase) {
        vertexMatches.add(match.vertex);
        tokenMatches.add(match.token);
      }

      put(vertexMatches, tokenMatches);

      excludedRanks = Sets.newHashSet(excludedRanks);
      excludedTokens = Sets.newHashSet(excludedTokens);

      excludedRanks.add(Ranges.closed(ranking.apply(vertexMatches.get(0)), ranking.apply(vertexMatches.get(vertexMatches.size() - 1))));
      excludedTokens.add(Ranges.closed(tokenMatches.get(0), tokenMatches.get(tokenMatches.size() - 1)));
    }

    final Multimap<Integer, MatchThread> matchThreads = HashMultimap.create();
    final SortedSetMultimap<Integer,VariantGraph.Vertex> rankMap = ranking.getByRank();

    for (Integer rank : rankMap.keySet()) {
      boolean rankExcluded = false;
      for (Range<Integer> rankRange : excludedRanks) {
        if (rankRange.contains(rank)) {
          rankExcluded = true;
          break;
        }
      }
      if (rankExcluded) {
        continue;
      }

      final SortedSet<VariantGraph.Vertex> vertices = rankMap.get(rank);

      for (VariantGraph.Vertex vertex : vertices) {
        final MatchThread matchThread = new MatchThread(suffixTree).advance(vertex, excludedTokens);
        if (matchThread != null) {
          matchThreads.put(rank, matchThread);
        }
      }
      for (MatchThread matchThread : matchThreads.get(rank - 1)) {
        for (VariantGraph.Vertex vertex : vertices) {
          final MatchThread advanced = matchThread.advance(vertex, excludedTokens);
          if (advanced != null) {
            matchThreads.put(rank, advanced);
            break;
          }
        }
      }
    }

    if (matchThreads.isEmpty()) {
      return this;
    }

    final Multimap<Integer,List<Match>> matchesByLength = Multimaps.index(
            Iterables.transform(matchThreads.values(), MatchThread.toMatches(excludedTokens)),
            TO_MATCH_LENGTH
    );

    final List<Integer> matchLengths = Ordering.natural().reverse().immutableSortedCopy(matchesByLength.keySet());
    for (Integer length : matchLengths) {
      final Collection<List<Match>> matches = matchesByLength.get(length);
      if (matches.size() == 1) {
        return find(ranking, excludedRanks, suffixTree, excludedTokens, Iterables.get(matches, 0));
      }

      List<Match> prev = null;
      for (List<Match> match : matches) {
        if (prev != null && prev.get(0).token != match.get(0).token) {
          return find(ranking, excludedRanks, suffixTree, excludedTokens, prev);
        }
      }
    }

    return find(ranking, excludedRanks, suffixTree, excludedTokens, Iterables.get(matchesByLength.get(Iterables.get(matchLengths, 0)), 0));
  }

  static final Function<List<Match>,Integer> TO_MATCH_LENGTH = new Function<List<Match>, Integer>() {
    @Override
    public Integer apply(@Nullable List<Match> input) {
      return input.size();
    }
  };

}
