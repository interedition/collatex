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

import com.google.common.base.Stopwatch;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ranges;
import com.google.common.collect.Sets;
import eu.interedition.collatex.CollationAlgorithm;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.dekker.Tuple;
import eu.interedition.collatex.util.VariantGraphRanking;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.logging.Level;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class MediteAlgorithm extends CollationAlgorithm.Base {

  private final Comparator<Token> comparator;

  public MediteAlgorithm(Comparator<Token> comparator) {
    this.comparator = comparator;
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
      for (Phrase<Match.WithTokenIndex> phrase : AlignmentDecisionGraph.filter(maximalUniqueMatches)) {
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

    // gather matched tokens into a list ordered by their natural order
    final List<Integer> sortedMatchedTokens = Lists.newArrayList();
    for (Phrase<Match.WithTokenIndex> phraseMatch : phraseMatches) {
      sortedMatchedTokens.add(phraseMatch.first().token);
    }
    Collections.sort(sortedMatchedTokens);

    // detect transpositions
    final List<Phrase<Match.WithTokenIndex>> transpositions = Lists.newArrayList();

    int previousToken = 0;
    Tuple<Integer> previous = new Tuple<Integer>(0, 0);

    for (Phrase<Match.WithTokenIndex> phraseMatch : phraseMatches) {
      int currentToken = phraseMatch.first().token;
      int expectedToken = sortedMatchedTokens.get(previousToken);
      Tuple<Integer> current = new Tuple<Integer>(expectedToken, currentToken);
      if (expectedToken != currentToken && !isMirrored(previous, current)) {
        transpositions.add(phraseMatch);
      }
      previousToken++;
      previous = current;
    }
    return transpositions;
  }

  private boolean isMirrored(Tuple<Integer> previousTuple, Tuple<Integer> tuple) {
    return previousTuple.left.equals(tuple.right) && previousTuple.right.equals(tuple.left);
  }

  String toString(Phrase<Match.WithTokenIndex> phrase, Token[] tokens) {
    final List<VariantGraph.Vertex> phraseVertices = Lists.newArrayListWithExpectedSize(phrase.size());
    final List<Token> phraseTokens = Lists.newArrayListWithExpectedSize(phrase.size());
    for (Match.WithTokenIndex tokenMatch : phrase) {
      phraseVertices.add(tokenMatch.vertex);
      phraseTokens.add(tokens[tokenMatch.token]);
    }
   return Iterables.toString(phraseVertices) + " == " + Iterables.toString(phraseTokens);
  }
}
