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

package eu.interedition.collatex;

import edu.uci.ics.jung.graph.DirectedSparseGraph;
import eu.interedition.collatex.util.VariantGraphTraversal;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class VariantGraph extends DirectedSparseGraph<VariantGraph.Vertex, VariantGraph.Edge> {
  final VariantGraph.Vertex start;
  final VariantGraph.Vertex end;
  final Map<Vertex, Set<Transposition>> transpositionIndex = new HashMap<>();

  public VariantGraph() {
    super();
    addVertex(this.start = new VariantGraph.Vertex(this, Collections.<Token>emptySet()));
    addVertex(this.end = new VariantGraph.Vertex(this, Collections.<Token>emptySet()));
    connect(this.start, this.end, Collections.<Witness>emptySet());
  }

  public Vertex getStart() {
    return start;
  }

  public Vertex getEnd() {
    return end;
  }

  public Set<Transposition> transpositions() {
    return transpositionIndex.values().stream().flatMap(Set::stream).collect(Collectors.toSet());
  }

  public Iterable<Vertex> vertices() {
    return vertices(null);
  }

  public Iterable<Vertex> vertices(Set<Witness> witnesses) {
    return VariantGraphTraversal.of(this, witnesses);
  }

  public Iterable<Edge> edges() {
    return edges(null);
  }

  public Iterable<Edge> edges(Set<Witness> witnesses) {
    return VariantGraphTraversal.of(this, witnesses).edges();
  }

  public Vertex add(Token token) {
    final VariantGraph.Vertex vertex = new VariantGraph.Vertex(this, Collections.singleton(token));
    addVertex(vertex);
    return vertex;
  }

  public Edge connect(Vertex from, Vertex to, Set<Witness> witnesses) {
    if (from.equals(to)) {
      throw new IllegalArgumentException();
    }

    if (from.equals(start)) {
      final Edge startEndEdge = edgeBetween(start, end);
      if (startEndEdge != null) {
        if (to.equals(end)) {
          witnesses = new HashSet<>(witnesses);
          witnesses.addAll(startEndEdge.witnesses());
        }
        startEndEdge.delete();
      }
    }

    for (Edge e : from.outgoing()) {
      if (to.equals(e.to())) {
        return e.add(witnesses);
      }
    }

    final VariantGraph.Edge edge = new VariantGraph.Edge(this, witnesses);
    addEdge(edge, from, to);
    return edge;
  }

  public Edge register(Witness witness) {
    return connect(start, end, Collections.singleton(witness));
  }

  public Transposition transpose(Set<Vertex> vertices) {
    if (vertices.isEmpty()) {
      throw new IllegalArgumentException();
    }
    for (Transposition transposition : vertices.iterator().next().transpositions()) {
      if (transposition.vertices.equals(vertices)) {
        return transposition;
      }
    }
    return new VariantGraph.Transposition(this, vertices);
  }

  public Edge edgeBetween(Vertex a, Vertex b) {
    return findEdge(a, b);
  }

  public Set<Witness> witnesses() {
    Set<Witness> witnesses = new HashSet<>();
    for (Edge edge : start.outgoing()) {
      witnesses.addAll(edge.witnesses());
    }
    return witnesses;
  }

  @Override
  public String toString() {
    return witnesses().toString();
  }


  /**
   * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
   */
  public static class Edge {

    final VariantGraph graph;
    final Set<Witness> witnesses;

    public Edge(VariantGraph graph, Set<Witness> witnesses) {
      this.graph = graph;
      this.witnesses = new HashSet<>(witnesses);
    }

    public VariantGraph.Edge add(Set<Witness> witnesses) {
      this.witnesses.addAll(witnesses);
      return this;
    }

    public Set<Witness> witnesses() {
      return Collections.unmodifiableSet(witnesses);
    }

    public VariantGraph graph() {
      return graph;
    }

    public VariantGraph.Vertex from() {
      return graph.getEndpoints(this).getFirst();
    }

    public VariantGraph.Vertex to() {
      return graph.getEndpoints(this).getSecond();
    }

    public void delete() {
      graph.removeEdge(this);
    }

    @Override
    public String toString() {
      return witnesses.toString();
    }

  }

  /**
   * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
   */
  public static class Vertex {
    private final VariantGraph graph;
    private final Set<Token> tokens;

    public Vertex(VariantGraph graph, Set<Token> tokens) {
      this.graph = graph;
      this.tokens = new HashSet<>(tokens);
    }

    public Collection<VariantGraph.Edge> incoming() {
      return incoming(null);
    }

    public Collection<VariantGraph.Edge> incoming(final Set<Witness> witnesses) {
      return paths(graph.getInEdges(this), witnesses);
    }

    public Collection<VariantGraph.Edge> outgoing() {
      return outgoing(null);
    }

    public Collection<VariantGraph.Edge> outgoing(Set<Witness> witnesses) {
      return paths(graph.getOutEdges(this), witnesses);
    }

    public Collection<Transposition> transpositions() {
      return graph.transpositionIndex.getOrDefault(this, Collections.emptySet());
    }

    public Set<Token> tokens() {
      return tokens(null);
    }

    public Set<Token> tokens(final Set<Witness> witnesses) {
      return Collections.unmodifiableSet(witnesses == null ? tokens :tokens.stream().filter(t -> witnesses.contains(t.getWitness())).collect(Collectors.toSet()));
    }

    public Set<Witness> witnesses() {
      final Set<Witness> witnesses = new HashSet<>();
      for (VariantGraph.Edge edge : incoming()) {
        witnesses.addAll(edge.witnesses());
      }
      return witnesses;
    }

    public void add(Iterable<Token> tokens) {
      tokens.forEach(this.tokens::add);
    }

    public VariantGraph graph() {
      return graph;
    }

    public void delete() {
      graph.removeVertex(this);
    }

    public String toString() {
      return tokens.toString();
    }

    protected static Collection<Edge> paths(final Collection<Edge> edges, final Set<Witness> witnesses) {
      if (witnesses == null) {
        return edges;
      }
      return Arrays.asList(edges.stream().filter(edge -> edge.witnesses().stream().anyMatch(witnesses::contains)).toArray(Edge[]::new));
    }
  }

  /**
   * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
   */
  public static class Transposition implements Iterable<Vertex> {
    private final VariantGraph graph;
    private final Set<VariantGraph.Vertex> vertices;

    public Transposition(VariantGraph graph, Set<VariantGraph.Vertex> vertices) {
      this.graph = graph;
      this.vertices = new HashSet<>(vertices);
      for (VariantGraph.Vertex vertex : this.vertices) {
        graph.transpositionIndex.computeIfAbsent(vertex, v -> new HashSet<>()).add(this);
      }
    }

    public void delete() {
      for (VariantGraph.Vertex vertex : this.vertices) {
        graph.transpositionIndex.getOrDefault(vertex, Collections.emptySet()).remove(this);
      }
    }

    @Override
    public Iterator<Vertex> iterator() {
      return vertices.iterator();
    }

    @Override
    public String toString() {
      return vertices.toString();
    }
  }

  public static final Function<VariantGraph, VariantGraph> JOIN = new Function<VariantGraph, VariantGraph>() {
    @Override
    public VariantGraph apply(VariantGraph graph) {
      final Set<Vertex> processed = new HashSet<>();

      final Vertex end = graph.getEnd();
      final Deque<Vertex> queue = new ArrayDeque<>();
      for (VariantGraph.Edge startingEdges : graph.getStart().outgoing()) {
        queue.push(startingEdges.to());
      }

      while (!queue.isEmpty()) {
        final Vertex vertex = queue.pop();
        final Set<Transposition> transpositions = new HashSet<>(vertex.transpositions());
        final List<Edge> outgoingEdges = new ArrayList<>(vertex.outgoing());
        if (outgoingEdges.size() == 1) {
          final Edge joinCandidateEdge = outgoingEdges.get(0);
          final Vertex joinCandidateVertex = joinCandidateEdge.to();
          final Set<Transposition> joinCandidateTranspositions = new HashSet<>(joinCandidateVertex.transpositions());

          boolean canJoin = !end.equals(joinCandidateVertex) && //
                  joinCandidateVertex.incoming().size() == 1 && //
                  transpositions.equals(joinCandidateTranspositions);
          if (canJoin) {
            vertex.add(joinCandidateVertex.tokens());
            for (Transposition t : new HashSet<>(joinCandidateVertex.transpositions())) {
              final Set<Vertex> transposed = new HashSet<>(t.vertices);
              transposed.remove(joinCandidateVertex);
              transposed.add(vertex);
              t.delete();
              graph.transpose(transposed);
            }
            for (Edge e : new ArrayList<>(joinCandidateVertex.outgoing())) {
              final Vertex to = e.to();
              final Set<Witness> witnesses = e.witnesses();
              e.delete();
              graph.connect(vertex, to, witnesses);
            }
            joinCandidateEdge.delete();
            joinCandidateVertex.delete();
            queue.push(vertex);
            continue;
          }
        }

        processed.add(vertex);
        for (Edge e : outgoingEdges) {
          final Vertex next = e.to();
          // FIXME: Why do we run out of memory in some cases here, if this is not checked?
          if (!processed.contains(next)) {
            queue.push(next);
          }
        }
      }

      return graph;
    }
  };
}
