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

import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;

import java.util.BitSet;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author <a href="http://gregor.middell.net/">Gregor Middell</a>
 */
public abstract class VertexMatch implements Comparable<VertexMatch> {
    public final VariantGraph.Vertex vertex;
    public final int vertexRank;

    VertexMatch(VariantGraph.Vertex vertex, int vertexRank) {
        this.vertex = vertex;
        this.vertexRank = vertexRank;
    }

    @Override
    public int compareTo(VertexMatch o) {
        return (vertexRank - o.vertexRank);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof VertexMatch) {
            return vertexRank == ((VertexMatch) obj).vertexRank;
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return vertexRank;
    }

    public static <T extends VertexMatch> Comparator<SortedSet<T>> setComparator() {
        return Comparator.comparing(SortedSet::first);
    }

    /**
     * @author <a href="http://gregor.middell.net/">Gregor Middell</a>
     */
    public static class WithToken extends VertexMatch {

        public final Token token;

        public WithToken(VariantGraph.Vertex vertex, int vertexRank, Token token) {
            super(vertex, vertexRank);
            this.token = token;
        }

        @Override
        public String toString() {
            return "{" + vertex + " -> " + token + "}";
        }
    }

    /**
     * @author <a href="http://gregor.middell.net/">Gregor Middell</a>
     */
    public static class WithTokenIndex extends VertexMatch {

        public final int token;

        public WithTokenIndex(VariantGraph.Vertex vertex, int vertexRank, int token) {
            super(vertex, vertexRank);
            this.token = token;
        }

        @Override
        public String toString() {
            return "{" + vertex + " -> " + token + "}";
        }
    }

    public static Function<WithTokenIndex, WithToken> tokenResolver(final Token[] tokens) {
        return input -> new WithToken(input.vertex, input.vertexRank, tokens[input.token]);
    }

    public static Predicate<SortedSet<WithTokenIndex>> filter(final BitSet rankFilter, final BitSet tokenFilter) {
        return input -> input.stream().anyMatch(match -> tokenFilter.get(match.token) || rankFilter.get(match.vertexRank));
    }
}
