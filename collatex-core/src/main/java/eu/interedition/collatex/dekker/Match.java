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

package eu.interedition.collatex.dekker;

import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author <a href="http://gregor.middell.net/">Gregor Middell</a>
 */
public class Match {
    public final VariantGraph.Vertex vertex;
    public final Token token;

    public Match(VariantGraph.Vertex vertex, Token token) {
        this.vertex = vertex;
        this.token = token;
    }

    @Override
    public int hashCode() {
        return Objects.hash(vertex, token);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof Match) {
            Match other = (Match) obj;
            return vertex.equals(other.vertex) && token.equals(other.token);
        }
        return super.equals(obj);
    }

    @Override
    public String toString() {
        return "{" + vertex + "; " + token + "}";
    }

    public static List<Match> createPhraseMatch(List<VariantGraph.Vertex> vertices, List<Token> tokens) {
        final List<Match> phraseMatch = new ArrayList<>(vertices.size());
        final Iterator<VariantGraph.Vertex> vertexIt = vertices.iterator();
        final Iterator<Token> tokenIt = tokens.iterator();
        while (vertexIt.hasNext() && tokenIt.hasNext()) {
            phraseMatch.add(new Match(vertexIt.next(), tokenIt.next()));
        }
        return phraseMatch;
    }


    public static Predicate<Match> createNoBoundaryMatchPredicate(final VariantGraph graph) {
        return input -> !input.vertex.equals(graph.getStart()) && !input.vertex.equals(graph.getEnd());
    }

    public static final Function<List<Match>, List<Token>> PHRASE_MATCH_TO_TOKENS =//
        input -> input.stream().map(m -> m.token).collect(Collectors.toList());
}
