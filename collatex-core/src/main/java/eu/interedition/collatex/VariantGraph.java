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

import eu.interedition.collatex.util.VariantGraphTraversal;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author <a href="http://gregor.middell.net/">Gregor Middell</a>
 */
public class VariantGraph {
    final VariantGraph.Vertex start;
    final VariantGraph.Vertex end;
    final Map<Vertex, Set<Set<Vertex>>> transpositionIndex = new HashMap<>();

    public VariantGraph() {
        super();
        this.start = new VariantGraph.Vertex(this);
        this.end = new VariantGraph.Vertex(this);

        this.start.outgoing.put(this.end, Collections.emptySet());
        this.end.incoming.put(this.start, Collections.emptySet());
    }

    public Vertex getStart() {
        return start;
    }

    public Vertex getEnd() {
        return end;
    }

    public Set<Set<Vertex>> transpositions() {
        return transpositionIndex.values().stream().flatMap(Set::stream).collect(Collectors.toSet());
    }

    public Iterable<Vertex> vertices() {
        return VariantGraphTraversal.of(this);
    }

    public Vertex add(Token token) {
        final VariantGraph.Vertex vertex = new VariantGraph.Vertex(this);
        vertex.tokens.add(token);
        return vertex;
    }

    public void connect(Vertex from, Vertex to, Set<Witness> witnesses) {
        if (from.equals(to)) {
            throw new IllegalArgumentException();
        }

        witnesses = new HashSet<>(witnesses);
        Optional.ofNullable(from.outgoing.remove(to)).ifPresent(witnesses::addAll);

        from.outgoing.put(to, witnesses);
        to.incoming.put(from, witnesses);

        start.outgoing.remove(end);
        end.incoming.remove(start);
    }

    public Set<Vertex> transpose(Set<Vertex> vertices) {
        if (vertices.isEmpty()) {
            throw new IllegalArgumentException();
        }
        for (Set<Vertex> transposition : vertices.iterator().next().transpositions()) {
            if (transposition.equals(vertices)) {
                return transposition;
            }
        }
        final Set<Vertex> t = new HashSet<>(vertices);
        for (VariantGraph.Vertex vertex : t) {
            transpositionIndex.computeIfAbsent(vertex, v -> new HashSet<>()).add(t);
        }
        return t;
    }

    public Set<Witness> witnesses() {
        return start.outgoing().values().stream().flatMap(Collection::stream).collect(Collectors.toSet());
    }

    @Override
    public String toString() {
        return witnesses().toString();
    }


    /**
     * @author <a href="http://gregor.middell.net/">Gregor Middell</a>
     */
    public static class Vertex {
        private final VariantGraph graph;
        private final Set<Token> tokens = new HashSet<>();
        private final Map<Vertex, Set<Witness>> outgoing = new HashMap<>();
        private final Map<Vertex, Set<Witness>> incoming = new HashMap<>();

        public Vertex(VariantGraph graph) {
            this.graph = graph;
        }

        public Map<Vertex, Set<Witness>> incoming() {
            return incoming;
        }

        public Map<Vertex, Set<Witness>> outgoing() {
            return outgoing;
        }

        public Set<Set<Vertex>> transpositions() {
            return graph.transpositionIndex.getOrDefault(this, Collections.emptySet());
        }

        public Set<Token> tokens() {
            return tokens;
        }

        public Set<Witness> witnesses() {
            return incoming().values().stream().flatMap(Set::stream).collect(Collectors.toSet());
        }

        public void add(Iterable<Token> tokens) {
            tokens.forEach(this.tokens::add);
        }

        public void clear() {
            outgoing.clear();
            incoming.clear();
        }

        public VariantGraph graph() {
            return graph;
        }

        public String toString() {
            return tokens.toString();
        }
    }

    public static final Function<VariantGraph, VariantGraph> JOIN = graph -> {
        final Set<Vertex> processed = new HashSet<>();
        final Deque<Vertex> queue = new ArrayDeque<>(graph.start.outgoing.keySet());

        while (!queue.isEmpty()) {
            final Vertex vertex = queue.pop();
            final Set<Set<Vertex>> transpositions = new HashSet<>(vertex.transpositions());
            if (vertex.outgoing.size() == 1) {
                final Vertex joinCandidateVertex = vertex.outgoing.keySet().iterator().next();
                final Set<Set<Vertex>> joinCandidateTranspositions = new HashSet<>(joinCandidateVertex.transpositions());

                boolean canJoin = !graph.end.equals(joinCandidateVertex) && //
                    joinCandidateVertex.incoming.size() == 1 && //
                    transpositions.equals(joinCandidateTranspositions);
                if (canJoin) {
                    vertex.add(joinCandidateVertex.tokens());
                    for (Set<Vertex> t : new HashSet<>(joinCandidateVertex.transpositions())) {
                        final Set<Vertex> transposed = new HashSet<>(t);
                        transposed.remove(joinCandidateVertex);
                        transposed.add(vertex);
                        for (Vertex tv : t) {
                            graph.transpositionIndex.getOrDefault(tv, Collections.emptySet()).remove(t);
                        }
                        graph.transpose(transposed);
                    }

                    vertex.outgoing.clear();
                    vertex.outgoing.putAll(joinCandidateVertex.outgoing);

                    vertex.outgoing.keySet().forEach(v -> v.incoming.put(vertex, v.incoming.remove(joinCandidateVertex)));

                    queue.push(vertex);
                    continue;
                }
            }

            // FIXME: Why do we run out of memory in some cases here, if this is not checked?
            processed.add(vertex);
            vertex.outgoing.keySet().stream().filter(v -> !processed.contains(v)).forEach(queue::push);
        }

        return graph;
    };
}
