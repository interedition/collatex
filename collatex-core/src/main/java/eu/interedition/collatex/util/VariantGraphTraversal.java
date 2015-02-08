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

package eu.interedition.collatex.util;

import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.Witness;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;

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
    return new Iterator<VariantGraph.Vertex>() {

      private final Map<VariantGraph.Vertex, Long> encountered = new HashMap<>();
      private final Queue<VariantGraph.Vertex> queue = new ArrayDeque<>();
      private Optional<VariantGraph.Vertex> next = Optional.of(graph.getStart());

      @Override
      public boolean hasNext() {
        return next.isPresent();
      }

      @Override
      public VariantGraph.Vertex next() {
        final VariantGraph.Vertex next = this.next.get();
        for (VariantGraph.Edge edge : next.outgoing()) {
          if (witnesses != null && !edge.witnesses().stream().anyMatch(witnesses::contains)) {
            continue;
          }
          final VariantGraph.Vertex end = edge.to();

          final long endEncountered = Optional.ofNullable(encountered.get(end)).orElse(0L);
          final long endIncoming = end.incoming().stream().filter(e -> witnesses == null || e.witnesses().stream().anyMatch(witnesses::contains)).count();

          if (endIncoming == endEncountered) {
            throw new IllegalStateException(String.format("Encountered cycle traversing %s to %s", edge, end));
          } else if ((endIncoming - endEncountered) == 1) {
            queue.add(end);
          }

          encountered.put(end, endEncountered + 1);
        }
        this.next = Optional.ofNullable(queue.poll());
        return next;
      }
    };
  }
}
