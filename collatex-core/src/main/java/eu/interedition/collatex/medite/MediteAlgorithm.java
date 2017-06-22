/*
 * Copyright (c) 2015 The Interedition Development Group.
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

import eu.interedition.collatex.CollationAlgorithm;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.util.StreamUtil;
import eu.interedition.collatex.util.VariantGraphRanking;
import eu.interedition.collatex.util.VertexMatch;

import java.util.BitSet;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.IntStream;

/**
 * @author <a href="http://gregor.middell.net/">Gregor Middell</a>
 */
public class MediteAlgorithm extends CollationAlgorithm.Base {

    private final Comparator<Token> comparator;
    private final Function<SortedSet<VertexMatch.WithToken>, Integer> matchEvaluator;

    public MediteAlgorithm(Comparator<Token> comparator, Function<SortedSet<VertexMatch.WithToken>, Integer> matchEvaluator) {
        this.comparator = comparator;
        this.matchEvaluator = matchEvaluator;
    }

    @Override
    public void collate(VariantGraph graph, Iterable<Token> witness) {
        final VariantGraph.Vertex[][] vertices = VariantGraphRanking.of(graph).asArray();
        final Token[] tokens = StreamUtil.stream(witness).toArray(Token[]::new);

        final SuffixTree<Token> suffixTree = SuffixTree.build(comparator, tokens);
        final MatchEvaluatorWrapper matchEvaluator = new MatchEvaluatorWrapper(this.matchEvaluator, tokens);

        final Matches matchCandidates = Matches.between(vertices, suffixTree, matchEvaluator);
        final SortedSet<SortedSet<VertexMatch.WithTokenIndex>> matches = new TreeSet<>(VertexMatch.<VertexMatch.WithTokenIndex>setComparator());

        while (true) {
            final SortedSet<SortedSet<VertexMatch.WithTokenIndex>> maximalUniqueMatches = matchCandidates.findMaximalUniqueMatches();
            if (maximalUniqueMatches.isEmpty()) {
                break;
            }

            final BitSet rankFilter = new BitSet();
            final BitSet tokenFilter = new BitSet();

            for (SortedSet<VertexMatch.WithTokenIndex> phrase : AlignmentDecisionGraph.filter(maximalUniqueMatches, matchEvaluator)) {
                final VertexMatch.WithTokenIndex firstMatch = phrase.first();
                final VertexMatch.WithTokenIndex lastMatch = phrase.last();

                matches.add(phrase);
                IntStream.range(firstMatch.vertexRank, lastMatch.vertexRank + 1).forEach(rankFilter::set);
                IntStream.range(firstMatch.token, lastMatch.token + 1).forEach(tokenFilter::set);
            }

            matchCandidates.removeIf(VertexMatch.filter(rankFilter, tokenFilter));
        }

        merge(graph, vertices, tokens, matches);
    }

    static class MatchEvaluatorWrapper implements Function<SortedSet<VertexMatch.WithTokenIndex>, Integer> {

        private final Function<SortedSet<VertexMatch.WithToken>, Integer> wrapped;
        private final Function<VertexMatch.WithTokenIndex, VertexMatch.WithToken> tokenResolver;

        MatchEvaluatorWrapper(final Function<SortedSet<VertexMatch.WithToken>, Integer> wrapped, final Token[] tokens) {
            this.wrapped = wrapped;
            this.tokenResolver = VertexMatch.tokenResolver(tokens);
        }

        @Override
        public Integer apply(SortedSet<VertexMatch.WithTokenIndex> input) {
            final SortedSet<VertexMatch.WithToken> tokenPhrase = new TreeSet<>();
            for (VertexMatch.WithTokenIndex match : input) {
                tokenPhrase.add(tokenResolver.apply(match));
            }
            return wrapped.apply(tokenPhrase);
        }
    }
}
