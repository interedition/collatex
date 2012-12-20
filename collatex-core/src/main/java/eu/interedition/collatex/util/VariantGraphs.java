package eu.interedition.collatex.util;

import com.google.common.base.Objects;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.BiMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Ordering;
import com.google.common.collect.RowSortedTable;
import com.google.common.collect.Sets;
import com.google.common.collect.TreeBasedTable;
import com.google.common.collect.TreeMultimap;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.Witness;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import static java.util.Collections.singleton;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class VariantGraphs {

  public static Multimap<Integer, VariantGraph.Vertex> rank(VariantGraph graph, Set<Witness> witnesses) {
    final Map<VariantGraph.Vertex, Integer> ranks = Maps.newHashMap();
    final Multimap<Integer, VariantGraph.Vertex> ranking = TreeMultimap.create(Ordering.natural(), Ordering.arbitrary());
    for (VariantGraph.Vertex v : graph.vertices(witnesses)) {
      int rank = -1;
      for (VariantGraph.Edge e : v.incoming(witnesses)) {
        rank = Math.max(rank, ranks.get(e.from()));
      }
      ranking.put(rank + 1, v);
      ranks.put(v, rank + 1);
    }
    return ranking;
  }

  public RowSortedTable<Integer, Witness, Set<Token>> table(VariantGraph graph, Set<Witness> witnesses) {
    final TreeBasedTable<Integer, Witness, Set<Token>> table = TreeBasedTable.create(Ordering.natural(), Witness.SIGIL_COMPARATOR);
    for (Map.Entry<Integer, VariantGraph.Vertex> rank : rank(graph, witnesses).entries()) {
      final int row = rank.getKey();
      for (Token token : rank.getValue().tokens(witnesses)) {
        final Witness column = token.getWitness();

        Set<Token> cell = table.get(row, column);
        if (cell == null) {
          table.put(row, column, cell = Sets.newHashSet());
        }
        cell.add(token);
      }
    }
    return table;
  }

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
}
