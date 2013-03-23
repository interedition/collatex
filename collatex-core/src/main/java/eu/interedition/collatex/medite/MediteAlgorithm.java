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
import com.google.common.base.Stopwatch;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ranges;
import com.google.common.collect.Sets;
import eu.interedition.collatex.CollationAlgorithm;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.needlemanwunsch.NeedlemanWunschAlgorithm;
import eu.interedition.collatex.needlemanwunsch.NeedlemanWunschScorer;
import eu.interedition.collatex.util.VariantGraphRanking;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.logging.Level;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class MediteAlgorithm extends CollationAlgorithm.Base {

  private final Comparator<Token> comparator;
  private final Function<Phrase<Match.WithToken>, Integer> matchEvaluator;

  public MediteAlgorithm(Comparator<Token> comparator, Function<Phrase<Match.WithToken>, Integer> matchEvaluator) {
    this.comparator = comparator;
    this.matchEvaluator = matchEvaluator;
  }

  @Override
  public void collate(VariantGraph graph, Iterable<Token> witness) {
    final boolean logTimings = LOG.isLoggable(Level.FINE);
    final Stopwatch stopwatch = (logTimings ? new Stopwatch() : null);

    if (logTimings) {
      stopwatch.start();
    }

    final Token[] tokens = Iterables.toArray(witness, Token.class);
    final SuffixTree<Token> suffixTree = SuffixTree.build(comparator, tokens);

    if (logTimings) {
      stopwatch.stop();
      LOG.log(Level.FINE, "Built suffix tree of {0} token(s) in {1}", new Object[] { tokens.length, stopwatch });
      stopwatch.reset().start();
    }

    final VariantGraphRanking ranking = VariantGraphRanking.of(graph);

    if (logTimings) {
      stopwatch.stop();
      LOG.log(Level.FINE, "Ranked variant graph {0} in {1}", new Object[] { graph, stopwatch });
      stopwatch.reset().start();
    }


    final MatchEvaluatorWrapper matchEvaluator = new MatchEvaluatorWrapper(this.matchEvaluator, ranking.size(), tokens);
    final Matches matcher = Matches.between(comparator, ranking, suffixTree);

    if (logTimings) {
      stopwatch.stop();
      LOG.log(Level.FINE, "Found a total of {0} matching phrase(s) in {1}", new Object[] { matcher.size(), stopwatch });
      stopwatch.reset().start();
    }

    final IndexRangeSet rankFilter = new IndexRangeSet();
    final IndexRangeSet tokenFilter = new IndexRangeSet();

    final SortedSet<Phrase<Match.WithTokenIndex>> alignments = Sets.newTreeSet();
    while (true) {
      final SortedSet<Phrase<Match.WithTokenIndex>> maximalUniqueMatches = matcher.removeMaximalUniqueMatches(rankFilter, tokenFilter);
      if (maximalUniqueMatches.isEmpty()) {
        break;
      }
      for (Phrase<Match.WithTokenIndex> phrase : AlignmentDecisionGraph.filter(maximalUniqueMatches, matchEvaluator)) {
        final Match.WithTokenIndex firstMatch = phrase.first();
        final Match.WithTokenIndex lastMatch = phrase.last();

        alignments.add(phrase);
        rankFilter.add(Ranges.closed(firstMatch.vertexRank, lastMatch.vertexRank));
        tokenFilter.add(Ranges.closed(firstMatch.token, lastMatch.token));
      }
    }

    if (logTimings) {
      stopwatch.stop();
      LOG.log(Level.FINE, "Selected {0} maximal unique matches in {1}", new Object[] { alignments.size(), stopwatch });
      stopwatch.reset().start();
    }

    final List<Phrase<Match.WithTokenIndex>> transpositions = transpositions(alignments);

    if (logTimings) {
      stopwatch.stop();
      LOG.log(Level.FINE, "Detected {0} transpositions in {1} maximal unique matches in {2}", new Object[] { transpositions.size(), alignments.size(), stopwatch });
      stopwatch.reset().start();
    }

    final Map<Token, VariantGraph.Vertex> tokenMatches = Maps.newHashMap();
    for (Phrase<Match.WithTokenIndex> phraseMatch : alignments) {
      for (Match.WithTokenIndex tokenMatch : phraseMatch) {
        tokenMatches.put(tokens[tokenMatch.token], tokenMatch.vertex);
      }
    }

    final List<List<eu.interedition.collatex.dekker.Match>> transpositionMatches = Lists.newLinkedList();
    for (Phrase<Match.WithTokenIndex> transposition : transpositions) {
      final List<eu.interedition.collatex.dekker.Match> transpositionMatch = Lists.newLinkedList();
      for (Match.WithTokenIndex match : transposition) {
        tokenMatches.remove(tokens[match.token]);
        transpositionMatch.add(new eu.interedition.collatex.dekker.Match(match.vertex, tokens[match.token]));
      }
      transpositionMatches.add(transpositionMatch);
    }

    merge(graph, witness, tokenMatches);
    mergeTranspositions(graph, transpositionMatches);

    if (logTimings) {
      stopwatch.stop();
      LOG.log(Level.FINE, "Merged {0} token matches and {1} transpositions in {2}", new Object[] { tokenMatches.size(), transpositions.size(), stopwatch });
    }
  }

  List<Phrase<Match.WithTokenIndex>> transpositions(SortedSet<Phrase<Match.WithTokenIndex>> phraseMatches) {

    final SortedSet<Integer> sortedMatchSet = Sets.newTreeSet();
    final Integer[] matches = new Integer[phraseMatches.size()];
    int tc = 0;
    for (Phrase<Match.WithTokenIndex> phraseMatch : phraseMatches) {
      sortedMatchSet.add(matches[tc++] = phraseMatch.first().token);
    }
    final Integer[] sortedMatches = sortedMatchSet.toArray(new Integer[sortedMatchSet.size()]);

    final Set<Integer> alignedMatches = NeedlemanWunschAlgorithm.align(
            sortedMatches,
            matches,
            new TranspositionAlignmentScorer(matches.length)
    ).keySet();

    final List<Phrase<Match.WithTokenIndex>> transpositions = Lists.newArrayList();
    for (Phrase<Match.WithTokenIndex> phraseMatch : phraseMatches) {
      if (!alignedMatches.contains(phraseMatch.first().token)) {
        transpositions.add(phraseMatch);
      }
    }
    return transpositions;
  }

  static class TranspositionAlignmentScorer implements NeedlemanWunschScorer<Integer, Integer> {

    final int maxPenality;

    TranspositionAlignmentScorer(int matchCount) {
      this.maxPenality = -matchCount;
    }

    @Override
    public float score(Integer a, Integer b) {
      return (a.equals(b) ? 1 : maxPenality);
    }

    @Override
    public float gap() {
      return -1;
    }
  }

  static class MatchEvaluatorWrapper implements Function<Phrase<Match.WithTokenIndex>, Integer> {

    final int maxDistance;
    private final Function<Phrase<Match.WithToken>, Integer> wrapped;
    private final Token[] tokens;

    MatchEvaluatorWrapper(final Function<Phrase<Match.WithToken>, Integer> wrapped, final int ranks, final Token[] tokens) {
      this.wrapped = wrapped;
      this.tokens = tokens;
      this.maxDistance = Math.max(ranks, tokens.length);
    }

    @Override
    public Integer apply(@Nullable Phrase<Match.WithTokenIndex> input) {
      Phrase<Match.WithToken> tokenPhrase = new Phrase<Match.WithToken>();
      for (Match.WithTokenIndex match : input) {
        tokenPhrase.add(new Match.WithToken(match.vertex, match.vertexRank, tokens[match.token]));
      }

      final Match.WithTokenIndex firstMatch = input.first();
      return (maxDistance * wrapped.apply(tokenPhrase)) + (maxDistance - Math.abs(firstMatch.vertexRank - firstMatch.token));
    }
  }
}
