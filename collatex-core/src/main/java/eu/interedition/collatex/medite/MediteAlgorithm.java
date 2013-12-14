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
import com.google.common.base.Joiner;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.common.collect.Range;
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
    final Stopwatch stopwatch = (logTimings ? Stopwatch.createStarted() : null);

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


    final MatchEvaluatorWrapper matchEvaluator = new MatchEvaluatorWrapper(this.matchEvaluator, tokens);
    final Matches matches = Matches.between(ranking, suffixTree, matchEvaluator);

    if (logTimings) {
      stopwatch.stop();
      LOG.log(Level.FINE, "Found a total of {0} matching phrase(s) in {1}", new Object[] { matches.size(), stopwatch });
      stopwatch.reset().start();
    }

    final SortedSet<Phrase<Match.WithTokenIndex>> alignments = Sets.newTreeSet();
    while (true) {
      final SortedSet<Phrase<Match.WithTokenIndex>> maximalUniqueMatches = matches.findMaximalUniqueMatches();
      if (maximalUniqueMatches.isEmpty()) {
        break;
      }

      final IndexRangeSet rankFilter = new IndexRangeSet();
      final IndexRangeSet tokenFilter = new IndexRangeSet();

      for (Phrase<Match.WithTokenIndex> phrase : AlignmentDecisionGraph.filter(maximalUniqueMatches, matchEvaluator)) {
        final Match.WithTokenIndex firstMatch = phrase.first();
        final Match.WithTokenIndex lastMatch = phrase.last();

        alignments.add(phrase);
        rankFilter.add(Range.closed(firstMatch.vertexRank, lastMatch.vertexRank));
        tokenFilter.add(Range.closed(firstMatch.token, lastMatch.token));
      }

      Iterables.removeIf(matches, Match.filter(rankFilter, tokenFilter));
    }

    if (logTimings) {
      stopwatch.stop();
      LOG.log(Level.FINE, "Selected {0} maximal unique matches in {1}\n{2}", new Object[]{
              alignments.size(), stopwatch, Joiner.on('\n').join(alignments)
      });
      stopwatch.reset().start();
    }

    final List<Phrase<Match.WithTokenIndex>> transpositions = transpositions(alignments, matchEvaluator, Math.max(tokens.length, ranking.size()));

    if (logTimings) {
      stopwatch.stop();
      LOG.log(Level.FINE, "Detected {0} transpositions in {1} maximal unique matches in {2}\n{3}", new Object[]{
              transpositions.size(), alignments.size(), stopwatch, Joiner.on('\n').join(transpositions)
      });
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

  @SuppressWarnings("unchecked")
  List<Phrase<Match.WithTokenIndex>> transpositions(SortedSet<Phrase<Match.WithTokenIndex>> phraseMatches, Function<Phrase<Match.WithTokenIndex>, Integer> matchEvaluator, int penalty) {
    final Phrase<Match.WithTokenIndex>[] vertexSorted = (Phrase<Match.WithTokenIndex>[]) phraseMatches.toArray(new Phrase[phraseMatches.size()]);
    final Phrase<Match.WithTokenIndex>[] tokenSorted = (Phrase<Match.WithTokenIndex>[]) Ordering.from(new Comparator<Phrase<Match.WithTokenIndex>>() {
      @Override
      public int compare(Phrase<Match.WithTokenIndex> o1, Phrase<Match.WithTokenIndex> o2) {
        return (o1.first().token - o2.first().token);
      }
    }).immutableSortedCopy(phraseMatches).toArray(new Phrase[phraseMatches.size()]);

    final Set<Phrase<Match.WithTokenIndex>> aligned = NeedlemanWunschAlgorithm.align(
            vertexSorted,
            tokenSorted,
            new TranspositionAlignmentScorer(matchEvaluator, penalty)
    ).keySet();

    final List<Phrase<Match.WithTokenIndex>> transpositions = Lists.newArrayList();
    for (Phrase<Match.WithTokenIndex> phraseMatch : phraseMatches) {
      if (!aligned.contains(phraseMatch)) {
        transpositions.add(phraseMatch);
      }
    }
    return transpositions;
  }

  static class TranspositionAlignmentScorer implements NeedlemanWunschScorer<Phrase<Match.WithTokenIndex>, Phrase<Match.WithTokenIndex>> {

    final Function<Phrase<Match.WithTokenIndex>, Integer> matchEvaluator;
    final int penalty;

    TranspositionAlignmentScorer(Function<Phrase<Match.WithTokenIndex>, Integer> matchEvaluator, int penalty) {
      this.matchEvaluator = matchEvaluator;
      this.penalty = penalty;
    }

    @Override
    public float score(Phrase<Match.WithTokenIndex> a, Phrase<Match.WithTokenIndex> b) {
      return (a.equals(b) ? 1 : -penalty);
    }

    @Override
    public float gap() {
      return -(1 / (penalty * 1.0f));
    }
  }

  static class MatchEvaluatorWrapper implements Function<Phrase<Match.WithTokenIndex>, Integer> {

    private final Function<Phrase<Match.WithToken>, Integer> wrapped;
    private final Function<Match.WithTokenIndex,Match.WithToken> tokenResolver;

    MatchEvaluatorWrapper(final Function<Phrase<Match.WithToken>, Integer> wrapped, final Token[] tokens) {
      this.wrapped = wrapped;
      this.tokenResolver = Match.tokenResolver(tokens);
    }

    @Override
    public Integer apply(@Nullable Phrase<Match.WithTokenIndex> input) {
      final Phrase<Match.WithToken> tokenPhrase = new Phrase<Match.WithToken>();
      for (Match.WithTokenIndex match : input) {
        tokenPhrase.add(tokenResolver.apply(match));
      }
      return wrapped.apply(tokenPhrase);
    }
  }
}
