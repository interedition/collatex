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

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import eu.interedition.collatex.util.VariantGraphTraversal;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class VariantGraph extends DirectedSparseGraph<VariantGraph.Vertex, VariantGraph.Edge> {
  final VariantGraph.Vertex start;
  final VariantGraph.Vertex end;
  final Multimap<Vertex, Transposition> transpositionIndex = HashMultimap.create();

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
    return Sets.newHashSet(transpositionIndex.values());
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
    Preconditions.checkArgument(!from.equals(to));

    if (from.equals(start)) {
      final Edge startEndEdge = edgeBetween(start, end);
      if (startEndEdge != null) {
        if (to.equals(end)) {
          witnesses = Sets.newHashSet(witnesses);
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
    Preconditions.checkArgument(!vertices.isEmpty());
    for (Transposition transposition : vertices.iterator().next().transpositions()) {
      if (Sets.newHashSet(transposition).equals(vertices)) {
        return transposition;
      }
    }
    return new VariantGraph.Transposition(this, vertices);
  }

  public Edge edgeBetween(Vertex a, Vertex b) {
    return findEdge(a, b);
  }

  public Set<Witness> witnesses() {
    Set<Witness> witnesses = Sets.newHashSet();
    for (Edge edge : start.outgoing()) {
      witnesses.addAll(edge.witnesses());
    }
    return witnesses;
  }

  @Override
  public String toString() {
    return Iterables.toString(witnesses());
  }


  /**
   * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
   */
  public static class Edge {

    final VariantGraph graph;
    final Set<Witness> witnesses;

    public Edge(VariantGraph graph, Set<Witness> witnesses) {
      this.graph = graph;
      this.witnesses = Sets.newHashSet(witnesses);
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
      return Iterables.toString(witnesses);
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
      this.tokens = Sets.newHashSet(tokens);
    }

    public Iterable<? extends VariantGraph.Edge> incoming() {
      return incoming(null);
    }

    public Iterable<? extends VariantGraph.Edge> incoming(final Set<Witness> witnesses) {
      return paths(graph.getInEdges(this), witnesses);
    }

    public Iterable<? extends VariantGraph.Edge> outgoing() {
      return outgoing(null);
    }

    public Iterable<? extends VariantGraph.Edge> outgoing(Set<Witness> witnesses) {
      return paths(graph.getOutEdges(this), witnesses);
    }

    public Iterable<? extends VariantGraph.Transposition> transpositions() {
      return graph.transpositionIndex.get(this);
    }

    public Set<Token> tokens() {
      return tokens(null);
    }

    public Set<Token> tokens(final Set<Witness> witnesses) {
      return Collections.unmodifiableSet(Sets.filter(tokens, witnesses == null ? Predicates.<Token>alwaysTrue() : new Predicate<Token>() {
        @Override
        public boolean apply(@Nullable Token token) {
          return witnesses.contains(token.getWitness());
        }
      }));
    }

    public Set<Witness> witnesses() {
      final Set<Witness> witnesses = Sets.newHashSet();
      for (VariantGraph.Edge edge : incoming()) {
        witnesses.addAll(edge.witnesses());
      }
      return witnesses;
    }

    public void add(Iterable<Token> tokens) {
      Iterables.addAll(this.tokens, tokens);
    }

    public VariantGraph graph() {
      return graph;
    }

    public void delete() {
      graph.removeVertex(this);
    }

    public String toString() {
      return Iterables.toString(tokens);
    }

    protected static Iterable<? extends VariantGraph.Edge> paths(final Iterable<VariantGraph.Edge> edges, final Set<Witness> witnesses) {
      return Iterables.filter(edges, (witnesses == null ? Predicates.<VariantGraph.Edge>alwaysTrue() : new Predicate<VariantGraph.Edge>() {
        @Override
        public boolean apply(@Nullable VariantGraph.Edge edge) {
          for (Witness edgeWitness : edge.witnesses()) {
            if (witnesses.contains(edgeWitness)) {
              return true;
            }
          }
          return false;
        }
      }));
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
      this.vertices = Sets.newHashSet(vertices);
      for (VariantGraph.Vertex vertex : this.vertices) {
        graph.transpositionIndex.put(vertex, this);
      }
    }

    public void delete() {
      for (VariantGraph.Vertex vertex : this.vertices) {
        graph.transpositionIndex.remove(vertex, this);
      }
    }

    @Override
    public Iterator<Vertex> iterator() {
      return vertices.iterator();
    }

    @Override
    public String toString() {
      return Iterables.toString(vertices);
    }
  }

  public static final Function<VariantGraph, VariantGraph> JOIN = new Function<VariantGraph, VariantGraph>() {
    @Override
    public VariantGraph apply(@Nullable VariantGraph graph) {
      final Set<Vertex> processed = Sets.newHashSet();

      final Vertex end = graph.getEnd();
      final Deque<Vertex> queue = new ArrayDeque<Vertex>();
      for (VariantGraph.Edge startingEdges : graph.getStart().outgoing()) {
        queue.push(startingEdges.to());
      }

      while (!queue.isEmpty()) {
        final Vertex vertex = queue.pop();
        final Set<Transposition> transpositions = Sets.newHashSet(vertex.transpositions());
        final List<Edge> outgoingEdges = Lists.newArrayList(vertex.outgoing());
        if (outgoingEdges.size() == 1) {
          final Edge joinCandidateEdge = outgoingEdges.get(0);
          final Vertex joinCandidateVertex = joinCandidateEdge.to();
          final Set<Transposition> joinCandidateTranspositions = Sets.newHashSet(joinCandidateVertex.transpositions());

          boolean canJoin = !end.equals(joinCandidateVertex) && //
                  Iterables.size(joinCandidateVertex.incoming()) == 1 && //
                  transpositions.equals(joinCandidateTranspositions);
          if (canJoin) {
            vertex.add(joinCandidateVertex.tokens());
            for (Transposition t : Sets.newHashSet(joinCandidateVertex.transpositions())) {
              final Set<Vertex> transposed = Sets.newHashSet(t);
              transposed.remove(joinCandidateVertex);
              transposed.add(vertex);
              t.delete();
              graph.transpose(transposed);
            }
            for (Edge e : Lists.newArrayList(joinCandidateVertex.outgoing())) {
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
