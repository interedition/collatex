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

package eu.interedition.collatex;

import eu.interedition.collatex.dekker.Match;
import eu.interedition.collatex.needlemanwunsch.NeedlemanWunschAlgorithm;
import eu.interedition.collatex.needlemanwunsch.NeedlemanWunschScorer;
import eu.interedition.collatex.util.StreamUtil;
import eu.interedition.collatex.util.VertexMatch;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author <a href="http://gregor.middell.net/">Gregor Middell</a>
 */
public interface CollationAlgorithm {

    void collate(VariantGraph against, Iterable<Token> witness);

    void collate(VariantGraph against, Iterable<Token>... witnesses);

    void collate(VariantGraph against, List<? extends Iterable<Token>> witnesses);

    abstract class Base implements CollationAlgorithm {
        protected final Logger LOG = Logger.getLogger(getClass().getName());
        protected Map<Token, VariantGraph.Vertex> witnessTokenVertices;

        @Override
        public void collate(VariantGraph against, Iterable<Token>... witnesses) {
            collate(against, Arrays.asList(witnesses));
        }

        @Override
        public void collate(VariantGraph against, List<? extends Iterable<Token>> witnesses) {
            for (Iterable<Token> witness : witnesses) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, "heap space: {0}/{1}", new Object[]{
                            Runtime.getRuntime().totalMemory(),
                            Runtime.getRuntime().maxMemory()
                    });
                }
                collate(against, witness);
            }
        }

        protected void merge(VariantGraph into, Iterable<Token> witnessTokens, Map<Token, VariantGraph.Vertex> alignments) {
            final Witness witness = StreamUtil.stream(witnessTokens)
                    .findFirst()
                    .map(Token::getWitness)
                    .orElseThrow(() -> new IllegalArgumentException("Empty witness"));

            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "{0} + {1}: Merge comparand into graph", new Object[]{into, witness});
            }
            witnessTokenVertices = new HashMap<>();
            VariantGraph.Vertex last = into.getStart();
            final Set<Witness> witnessSet = Collections.singleton(witness);
            for (Token token : witnessTokens) {
                VariantGraph.Vertex matchingVertex = alignments.get(token);
                if (matchingVertex == null) {
                    matchingVertex = into.add(token);
                } else {
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.log(Level.FINE, "Match: {0} to {1}", new Object[]{matchingVertex, token});
                    }
                    matchingVertex.add(Collections.singleton(token));
                }
                witnessTokenVertices.put(token, matchingVertex);

                into.connect(last, matchingVertex, witnessSet);
                last = matchingVertex;
            }
            into.connect(last, into.getEnd(), witnessSet);
        }

        protected void mergeTranspositions(VariantGraph into, Iterable<SortedSet<VertexMatch.WithToken>> transpositions) {
            for (SortedSet<VertexMatch.WithToken> transposedPhrase : transpositions) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, "Transposition: {0}", transposedPhrase);
                }
                final Set<VariantGraph.Vertex> transposed = new HashSet<>();
                for (VertexMatch.WithToken match : transposedPhrase) {
                    transposed.add(witnessTokenVertices.get(match.token));
                    transposed.add(match.vertex);
                }
                into.transpose(transposed);
            }
        }

        protected void mergeTranspositions(VariantGraph into, List<List<Match>> transpositions) {
            for (List<Match> transposedPhrase : transpositions) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, "Transposition: {0}", transposedPhrase);
                }
                final Set<VariantGraph.Vertex> transposed = new HashSet<>();
                for (Match match : transposedPhrase) {
                    transposed.add(witnessTokenVertices.get(match.token));
                    transposed.add(match.vertex);
                }
                into.transpose(transposed);
            }
        }

        protected void merge(VariantGraph graph, VariantGraph.Vertex[][] vertices, Token[] tokens, SortedSet<SortedSet<VertexMatch.WithTokenIndex>> matches) {
            @SuppressWarnings("unchecked") final SortedSet<VertexMatch.WithTokenIndex>[] matchesVertexOrder = matches.toArray(new SortedSet[matches.size()]);
            final SortedSet<VertexMatch.WithTokenIndex>[] matchesTokenOrder = Arrays.copyOf(matchesVertexOrder, matchesVertexOrder.length);

            Arrays.sort(matchesTokenOrder, Comparator.comparing(m -> m.first().token));

            final Set<SortedSet<VertexMatch.WithTokenIndex>> alignedMatches = NeedlemanWunschAlgorithm.align(
                    matchesVertexOrder,
                    matchesTokenOrder,
                    new MatchPhraseAlignmentScorer(Math.max(tokens.length, vertices.length))
            ).keySet();

            final Map<Token, VariantGraph.Vertex> alignments = matches.stream()
                    .filter(alignedMatches::contains)
                    .flatMap(Set::stream)
                    .collect(Collectors.toMap(m -> tokens[m.token], m -> m.vertex));

            final List<SortedSet<VertexMatch.WithToken>> transpositions = matches.stream()
                    .filter(m -> !alignedMatches.contains(m))
                    .map(t -> t.stream().map(m -> new VertexMatch.WithToken(m.vertex, m.vertexRank, tokens[m.token])).collect(Collectors.toCollection(TreeSet::new)))
                    .collect(Collectors.toList());

            merge(graph, Arrays.asList(tokens), alignments);
            mergeTranspositions(graph, transpositions);
        }
    }

    class MatchPhraseAlignmentScorer implements NeedlemanWunschScorer<SortedSet<VertexMatch.WithTokenIndex>, SortedSet<VertexMatch.WithTokenIndex>> {

        private final int maxWitnessLength;

        public MatchPhraseAlignmentScorer(int maxWitnessLength) {
            this.maxWitnessLength = maxWitnessLength;
        }

        @Override
        public float score(SortedSet<VertexMatch.WithTokenIndex> a, SortedSet<VertexMatch.WithTokenIndex> b) {
            return (a.equals(b) ? 1 : -maxWitnessLength);
        }

        @Override
        public float gap() {
            return -(1 / (maxWitnessLength * 1.0f));
        }

    }
}
