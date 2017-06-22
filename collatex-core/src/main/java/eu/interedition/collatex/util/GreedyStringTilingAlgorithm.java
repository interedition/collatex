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

package eu.interedition.collatex.util;

import eu.interedition.collatex.CollationAlgorithm;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;

import java.util.*;


/**
 * Greedy String Tiling.
 * <p>
 * Implements the Greedy String Tiling algorithm as proposed by Michael J. Wise in his paper:
 * "String Similarity via Greedy String Tiling and Running Karp-Rabin Matching"
 *
 * @author Arno Mittelbach
 * @author Lasse Lehmann
 * @author Gregor Middell
 */
public class GreedyStringTilingAlgorithm extends CollationAlgorithm.Base {

    private final Comparator<Token> comparator;
    private final int minimumTileLength;

    private final Equality<VariantGraph.Vertex[], Token> equality = new Equality<VariantGraph.Vertex[], Token>() {

        @Override
        public boolean isEqual(VariantGraph.Vertex[] a, Token b) {
            for (VariantGraph.Vertex vertex : a) {
                final Set<Token> tokens = vertex.tokens();
                if (!tokens.isEmpty() && comparator.compare(tokens.stream().findFirst().get(), b) == 0) {
                    return true;
                }
            }
            return false;
        }
    };

    public GreedyStringTilingAlgorithm(Comparator<Token> comparator, int minimumTileLength) {
        this.comparator = comparator;
        this.minimumTileLength = minimumTileLength;
    }

    @Override
    public void collate(VariantGraph graph, Iterable<Token> witness) {
        final VariantGraph.Vertex[][] vertices = VariantGraphRanking.of(graph).asArray();
        final Token[] tokens = StreamUtil.stream(witness).toArray(Token[]::new);

        final SortedSet<SortedSet<VertexMatch.WithTokenIndex>> matches = new TreeSet<>(VertexMatch.<VertexMatch.WithTokenIndex>setComparator());
        for (Match match : match(vertices, tokens, equality, minimumTileLength)) {
            final SortedSet<VertexMatch.WithTokenIndex> phrase = new TreeSet<>();
            for (int mc = 0, ml = match.length; mc < ml; mc++) {
                final int rank = match.left + mc;
                phrase.add(new VertexMatch.WithTokenIndex(vertices[rank][0], rank, match.right + mc));
            }
            matches.add(phrase);
        }

        merge(graph, vertices, tokens, matches);
    }

    public static <A, B> SortedSet<Match> match(A[] left, B[] right, Equality<A, B> equality, int minimumTileLength) {
        final boolean[] markedLeft = new boolean[left.length];
        final boolean[] markedRight = new boolean[right.length];

        Arrays.fill(markedLeft, false);
        Arrays.fill(markedRight, false);

        final SortedSet<Match> matches = new TreeSet<>();
        final Map<Integer, List<Match>> matchesByLength = new HashMap<>();

        int maxMatchLength;
        do {
            maxMatchLength = minimumTileLength;
            for (int rc = 0; rc < right.length; rc++) {
                for (int lc = 0; lc < left.length; lc++) {
                    int matchLength = 0;
                    for (int tc = 0;
                         (tc + lc) < left.length && (tc + rc) < right.length &&
                                 !markedLeft[lc + tc] && !markedRight[rc + tc] &&
                                 equality.isEqual(left[lc + tc], right[rc + tc]);
                         tc++) {
                        matchLength++;
                    }

                    if (matchLength >= maxMatchLength) {
                        List<Match> theMatches = matchesByLength.computeIfAbsent(matchLength, k -> new ArrayList<>());
                        theMatches.add(new Match(lc, rc));
                    }

                    if (matchLength > maxMatchLength) {
                        maxMatchLength = matchLength;
                    }
                }
            }

            for (Match match : matchesByLength.getOrDefault(maxMatchLength, Collections.emptyList())) {
                boolean occluded = false;

                for (int tc = 0; tc < maxMatchLength; tc++) {
                    if (markedLeft[match.left + tc] || markedRight[match.right + tc]) {
                        occluded = true;
                        break;
                    }
                }

                if (!occluded) {
                    for (int tc = 0; tc < maxMatchLength; tc++) {
                        markedLeft[match.left + tc] = true;
                        markedRight[match.right + tc] = true;
                    }
                    matches.add(new Match(match.left, match.right, maxMatchLength));
                }
            }

        } while (maxMatchLength > minimumTileLength);

        return matches;
    }

    public interface Equality<A, B> {
        boolean isEqual(A a, B b);
    }

    public static class Match implements Comparable<Match> {
        public final int left;
        public final int right;
        public final int length;

        public Match(int left, int right, int length) {
            this.left = left;
            this.right = right;
            this.length = length;
        }

        public Match(int left, int right) {
            this(left, right, 0);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj != null && obj instanceof Match) {
                return (left == ((Match) obj).left);
            }
            return super.equals(obj);
        }

        @Override
        public int hashCode() {
            return left;
        }

        @Override
        public int compareTo(Match o) {
            return left - o.left;
        }
    }
}
