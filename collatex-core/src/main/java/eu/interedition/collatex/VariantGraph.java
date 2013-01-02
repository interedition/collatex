package eu.interedition.collatex;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Set;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public interface VariantGraph {
  Vertex getStart();

  Vertex getEnd();

  Set<Transposition> transpositions();

  Iterable<Vertex> vertices();

  Iterable<Vertex> vertices(Set<Witness> witnesses);

  Iterable<Edge> edges();

  Iterable<Edge> edges(Set<Witness> witnesses);

  Vertex add(Token token);

  Edge connect(Vertex from, Vertex to, Set<Witness> witnesses);

  Transposition transpose(Set<Vertex> vertices);

  Edge edgeBetween(Vertex a, Vertex b);

  Set<Witness> witnesses();

  /**
   * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
   */
  interface Edge {

    VariantGraph graph();

    Edge add(Set<Witness> witnesses);

    Set<Witness> witnesses();

    Vertex from();

    Vertex to();

    void delete();
  }

  /**
   * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
   */
  interface Vertex {
    Iterable<? extends Edge> incoming();

    Iterable<? extends Edge> incoming(Set<Witness> witnesses);

    Iterable<? extends Edge> outgoing();

    Iterable<? extends Edge> outgoing(Set<Witness> witnesses);

    Iterable<? extends Transposition> transpositions();

    Set<Token> tokens();

    Set<Token> tokens(Set<Witness> witnesses);

    Set<Witness> witnesses();

    void add(Iterable<Token> tokens);

    VariantGraph graph();

    void delete();
  }

  /**
   * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
   */
  interface Transposition extends Iterable<Vertex> {
    void delete();
  }

  final Function<VariantGraph, VariantGraph> JOIN = new Function<VariantGraph, VariantGraph>() {
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
