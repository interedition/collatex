package eu.interedition.collatex.util;

import com.google.common.base.Objects;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
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
public class VariantGraphTraversal implements Iterable<VariantGraph.Vertex> {
  private final VariantGraph graph;
  private final Set<Witness> witnesses;

  private VariantGraphTraversal(VariantGraph graph, Set<Witness> witnesses) {
    this.graph = graph;
    this.witnesses = witnesses;
  }

  public static VariantGraphTraversal of(VariantGraph graph, Set<Witness> witnesses) {
    return new VariantGraphTraversal(graph, witnesses);
  }

  public static VariantGraphTraversal of(VariantGraph graph) {
    return new VariantGraphTraversal(graph, null);
  }

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

  public Iterable<VariantGraph.Edge> edges() {
    return new Iterable<VariantGraph.Edge>() {

      @Override
      public Iterator<VariantGraph.Edge> iterator() {
        return new AbstractIterator<VariantGraph.Edge>() {
          private final Iterator<VariantGraph.Vertex> vertexIt = VariantGraphTraversal.this.iterator();
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
