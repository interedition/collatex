package eu.interedition.collatex.util;

import com.google.common.base.Objects;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.BiMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Ordering;
import com.google.common.collect.RowSortedTable;
import com.google.common.collect.Sets;
import com.google.common.collect.SortedSetMultimap;
import com.google.common.collect.TreeBasedTable;
import com.google.common.collect.TreeMultimap;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.Witness;
import eu.interedition.collatex.neo4j.Neo4jVariantGraphVertex;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import static java.util.Collections.singleton;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class VariantGraphs {

  public static Iterable<VariantGraph.Vertex> vertices(final VariantGraph graph, final Set<Witness> witnesses) {
    return new Iterable<VariantGraph.Vertex>() {
      @Override
      public Iterator<VariantGraph.Vertex> iterator() {
        return new AbstractIterator<VariantGraph.Vertex>() {
          private final Map<VariantGraph.Vertex, Integer> encountered = Maps.newHashMap();
          private final Queue<VariantGraph.Vertex> queue = new ArrayDeque<VariantGraph.Vertex>(singleton(graph.getStart()));

          @Override
          protected VariantGraph.Vertex computeNext() {
            if (queue.isEmpty()) {
              return endOfData();
            }
            final VariantGraph.Vertex next = queue.remove();
            for (VariantGraph.Edge edge : next.outgoing(witnesses)) {
              final VariantGraph.Vertex end = edge.to();

              final int endEncountered = Objects.firstNonNull(encountered.get(end), 0);
              final int endIncoming = Iterables.size(end.incoming(witnesses));

              if (endIncoming == endEncountered) {
                throw new IllegalStateException(String.format("Encountered cycle traversing %s to %s", edge, end));
              } else if ((endIncoming - endEncountered) == 1) {
                queue.add(end);
              }

              encountered.put(end, endEncountered + 1);
            }
            return next;
          }
        };
      }
    };
  }

  public static Iterable<VariantGraph.Edge> edges(final VariantGraph graph, final Set<Witness> witnesses) {
    return new Iterable<VariantGraph.Edge>() {
      private final Iterable<VariantGraph.Vertex> vertices = vertices(graph, witnesses);

      @Override
      public Iterator<VariantGraph.Edge> iterator() {
        return new AbstractIterator<VariantGraph.Edge>() {
          private final Iterator<VariantGraph.Vertex> vertexIt = vertices.iterator();
          private final Queue<VariantGraph.Edge> queue = new ArrayDeque<VariantGraph.Edge>();

          @Override
          protected VariantGraph.Edge computeNext() {
            if (queue.isEmpty()) {
              if (vertexIt.hasNext()) {
                Iterables.addAll(queue, vertexIt.next().outgoing(witnesses));
              }
            }
            return (queue.isEmpty() ? endOfData() : queue.remove());
          }
        };
      }
    };
  }

  public static VariantGraph join(VariantGraph graph) {
    final Set<VariantGraph.Vertex> processed = Sets.newHashSet();

    final VariantGraph.Vertex end = graph.getEnd();
    final Deque<VariantGraph.Vertex> queue = new ArrayDeque<VariantGraph.Vertex>();
    for (VariantGraph.Edge startingEdges : graph.getStart().outgoing()) {
      queue.push(startingEdges.to());
    }

    while (!queue.isEmpty()) {
      final VariantGraph.Vertex vertex = queue.pop();
      final Set<VariantGraph.Transposition> transpositions = Sets.newHashSet(vertex.transpositions());
      final List<VariantGraph.Edge> outgoingEdges = Lists.newArrayList(vertex.outgoing());
      if (outgoingEdges.size() == 1) {
        final VariantGraph.Edge joinCandidateEdge = outgoingEdges.get(0);
        final VariantGraph.Vertex joinCandidateVertex = joinCandidateEdge.to();
        final Set<VariantGraph.Transposition> joinCandidateTranspositions = Sets.newHashSet(joinCandidateVertex.transpositions());

        boolean canJoin = !end.equals(joinCandidateVertex) && //
                Iterables.size(joinCandidateVertex.incoming()) == 1 && //
                transpositions.equals(joinCandidateTranspositions);
        if (canJoin) {
          vertex.add(joinCandidateVertex.tokens());
          for (VariantGraph.Transposition t : Sets.newHashSet(joinCandidateVertex.transpositions())) {
            final Set<VariantGraph.Vertex> transposed = Sets.newHashSet(t);
            transposed.remove(joinCandidateVertex);
            transposed.add(vertex);
            t.delete();
            graph.transpose(transposed);
          }
          for (VariantGraph.Edge e : Lists.newArrayList(joinCandidateVertex.outgoing())) {
            final VariantGraph.Vertex to = e.to();
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
      for (VariantGraph.Edge e : outgoingEdges) {
        final VariantGraph.Vertex next = e.to();
        // FIXME: Why do we run out of memory in some cases here, if this is not checked?
        if (!processed.contains(next)) {
          queue.push(next);
        }
      }
    }

    return graph;
  }
}
