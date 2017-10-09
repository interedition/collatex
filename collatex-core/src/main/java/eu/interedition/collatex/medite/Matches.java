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

import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.util.VertexMatch;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author <a href="http://gregor.middell.net/">Gregor Middell</a>
 */
public class Matches extends ArrayList<SortedSet<VertexMatch.WithTokenIndex>> {

    public Matches(int initialCapacity) {
        super(initialCapacity);
    }

    public static Matches between(VariantGraph.Vertex[][] vertices, SuffixTree<Token> suffixTree, Function<SortedSet<VertexMatch.WithTokenIndex>, Integer> matchEvaluator) {

        final Map<Integer, List<MatchThreadElement>> matchThreads = new HashMap<>();
        for (int rank = 0; rank < vertices.length; rank++) {
            for (VariantGraph.Vertex vertex : vertices[rank]) {
                final MatchThreadElement matchThreadElement = new MatchThreadElement(suffixTree).advance(vertex, rank);
                if (matchThreadElement != null) {
                    matchThreads.computeIfAbsent(rank, r -> new LinkedList<>()).add(matchThreadElement);
                }
            }
            for (MatchThreadElement matchThreadElement : matchThreads.getOrDefault(rank - 1, Collections.emptyList())) {
                for (VariantGraph.Vertex vertex : vertices[rank]) {
                    final MatchThreadElement advanced = matchThreadElement.advance(vertex, rank);
                    if (advanced != null) {
                        matchThreads.computeIfAbsent(rank, r -> new LinkedList<>()).add(advanced);
                    }
                }
            }
        }

        final Matches matches = new Matches(matchThreads.size());
        matchThreads.values().stream().flatMap(List::stream).forEach(matchThreadElement -> {
            final List<SortedSet<VertexMatch.WithTokenIndex>> threadPhrases = new ArrayList<>();
            boolean firstElement = true;
            for (MatchThreadElement threadElement : matchThreadElement.thread()) {
                final SuffixTree<Token>.EquivalenceClass equivalenceClass = threadElement.cursor.matchedClass();
                for (int mc = 0; mc < equivalenceClass.length; mc++) {
                    final int tokenCandidate = equivalenceClass.members[mc];
                    if (firstElement) {
                        final SortedSet<VertexMatch.WithTokenIndex> phrase = new TreeSet<>();
                        phrase.add(new VertexMatch.WithTokenIndex(threadElement.vertex, threadElement.vertexRank, tokenCandidate));
                        threadPhrases.add(phrase);
                    } else {
                        for (SortedSet<VertexMatch.WithTokenIndex> phrase : threadPhrases) {
                            if ((phrase.last().token + 1) == tokenCandidate) {
                                phrase.add(new VertexMatch.WithTokenIndex(threadElement.vertex, threadElement.vertexRank, tokenCandidate));
                            }
                        }
                    }
                }
                firstElement = false;
            }
            matches.addAll(threadPhrases);
        });
        matches.sort(maximalUniqueMatchOrdering(matchEvaluator));

        return matches;
    }

    private static Comparator<SortedSet<VertexMatch.WithTokenIndex>> maximalUniqueMatchOrdering(final Function<SortedSet<VertexMatch.WithTokenIndex>, Integer> matchEvaluator) {
        return (o1, o2) -> {
            // 1. reverse ordering by match value
            int result = matchEvaluator.apply(o2) - matchEvaluator.apply(o1);
            if (result != 0) {
                return result;
            }

            final VertexMatch.WithTokenIndex firstMatch1 = o1.first();
            final VertexMatch.WithTokenIndex firstMatch2 = o2.first();

            // 2. ordering by match distance
            result = (Math.abs(firstMatch1.token - firstMatch1.vertexRank) - Math.abs(firstMatch2.token - firstMatch2.vertexRank));
            if (result != 0) {
                return result;
            }


            // 3. ordering by first vertex ranking
            result = firstMatch1.vertexRank - firstMatch2.vertexRank;
            if (result != 0) {
                return result;
            }

            // 3. ordering by first token index
            return firstMatch1.token - firstMatch2.token;

        };
    }

    public SortedSet<SortedSet<VertexMatch.WithTokenIndex>> findMaximalUniqueMatches() {
        final List<SortedSet<VertexMatch.WithTokenIndex>> allMatches = new ArrayList<>(this);
        final SortedSet<SortedSet<VertexMatch.WithTokenIndex>> maximalUniqueMatches = new TreeSet<>(VertexMatch.<VertexMatch.WithTokenIndex>setComparator());

        while (true) {
            SortedSet<VertexMatch.WithTokenIndex> nextMum = null;
            SortedSet<VertexMatch.WithTokenIndex> candidate = null;
            for (SortedSet<VertexMatch.WithTokenIndex> successor : allMatches) {
                if (candidate == null) {
                    continue;
                }
                if (candidate.size() > successor.size() || candidate.first().token == successor.first().token) {
                    nextMum = candidate;
                    break;
                }
                candidate = successor;
            }
            if (nextMum == null) {
                nextMum = allMatches.stream().findFirst().orElse(null);
            }
            if (nextMum == null) {
                break;
            }
            if (!maximalUniqueMatches.add(nextMum)) {
                throw new IllegalStateException("Duplicate MUM");
            }

            final BitSet rankFilter = new BitSet();
            final BitSet tokenFilter = new BitSet();

            rankFilter.set(nextMum.first().vertexRank, nextMum.last().vertexRank + 1);
            tokenFilter.set(nextMum.first().token, nextMum.last().token + 1);

            allMatches.removeIf(VertexMatch.filter(rankFilter, tokenFilter));
        }
        return maximalUniqueMatches;
    }

    /**
     * @author <a href="http://gregor.middell.net/">Gregor Middell</a>
     */
    static class MatchThreadElement {

        final MatchThreadElement previous;
        final VariantGraph.Vertex vertex;
        final int vertexRank;
        final SuffixTree<Token>.Cursor cursor;

        MatchThreadElement(SuffixTree<Token> suffixTree) {
            this(null, null, -1, suffixTree.cursor());
        }

        MatchThreadElement(MatchThreadElement previous, VariantGraph.Vertex vertex, int vertexRank, SuffixTree<Token>.Cursor cursor) {
            this.previous = previous;
            this.vertex = vertex;
            this.vertexRank = vertexRank;
            this.cursor = cursor;
        }

        MatchThreadElement advance(VariantGraph.Vertex vertex, int vertexRank) {
            final Set<Token> tokens = vertex.tokens();
            if (!tokens.isEmpty()) {
                final SuffixTree<Token>.Cursor next = cursor.move(tokens.stream().findFirst().get());
                if (next != null) {
                    return new MatchThreadElement(this, vertex, vertexRank, next);
                }
            }
            return null;
        }

        List<MatchThreadElement> thread() {
            final LinkedList<MatchThreadElement> thread = new LinkedList<>();
            MatchThreadElement current = this;
            while (current.vertex != null) {
                thread.addFirst(current);
                current = current.previous;
            }
            return thread;
        }

        @Override
        public String toString() {
            return "[" + Stream.of(vertexRank, vertex, cursor.matchedClass()).map(Object::toString).collect(Collectors.joining(", ")) + "]";
        }
    }
}
