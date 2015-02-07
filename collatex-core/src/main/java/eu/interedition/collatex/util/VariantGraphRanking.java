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

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Ordering;
import com.google.common.collect.RowSortedTable;
import com.google.common.collect.Sets;
import com.google.common.collect.SortedSetMultimap;
import com.google.common.collect.TreeBasedTable;
import com.google.common.collect.TreeMultimap;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.VariantGraph.Vertex;
import eu.interedition.collatex.Witness;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 * @author Ronald Haentjens Dekker
 */
public class VariantGraphRanking implements Iterable<Set<VariantGraph.Vertex>>, Function<VariantGraph.Vertex,Integer>, Comparator<VariantGraph.Vertex> {

  private final Map<VariantGraph.Vertex, Integer> byVertex = Maps.newHashMap();
  private final SortedSetMultimap<Integer, VariantGraph.Vertex> byRank = TreeMultimap.create(Ordering.natural(), Ordering.arbitrary());
  private final VariantGraph graph;
  private final Set<Witness> witnesses;

  VariantGraphRanking(VariantGraph graph, Set<Witness> witnesses) {
    this.graph = graph;
    this.witnesses = witnesses;
  }

  public static VariantGraphRanking of(VariantGraph graph) {
    return of(graph, null);
  }

  public static VariantGraphRanking of(VariantGraph graph, Set<Witness> witnesses) {
    final VariantGraphRanking ranking = new VariantGraphRanking(graph, witnesses);
    for (VariantGraph.Vertex v : graph.vertices(witnesses)) {
      int rank = -1;
      for (VariantGraph.Edge e : v.incoming(witnesses)) {
        rank = Math.max(rank, ranking.byVertex.get(e.from()));
      }
      rank++;
      ranking.byVertex.put(v, rank);
      ranking.byRank.put(rank, v);
    }
    return ranking;
  }

  public static VariantGraphRanking ofOnlyCertainVertices(VariantGraph graph, Set<Witness> witnesses, Set<VariantGraph.Vertex> vertices) {
    final VariantGraphRanking ranking = new VariantGraphRanking(graph, witnesses);
    for (VariantGraph.Vertex v : graph.vertices(witnesses)) {
      int rank = -1;
      for (VariantGraph.Edge e : v.incoming(witnesses)) {
        rank = Math.max(rank, ranking.byVertex.get(e.from()));
      }
      if (vertices.contains(v)) {
        rank++;
      }
      ranking.byVertex.put(v, rank);
      ranking.byRank.put(rank, v);
    }
    return ranking;
  }

  public Set<Witness> witnesses() {
    return Objects.firstNonNull(witnesses, graph.witnesses());
  }

  public Map<VariantGraph.Vertex, Integer> getByVertex() {
    return Collections.unmodifiableMap(byVertex);
  }

  public SortedSetMultimap<Integer, VariantGraph.Vertex> getByRank() {
    return Multimaps.unmodifiableSortedSetMultimap(byRank);
  }

  public int size() {
    return byRank.keySet().size();
  }

  @Override
  public Iterator<Set<VariantGraph.Vertex>> iterator() {
    return new AbstractIterator<Set<VariantGraph.Vertex>>() {
      private final Iterator<Integer> it = byRank.keySet().iterator();

      @Override
      protected Set<VariantGraph.Vertex> computeNext() {
        return (it.hasNext() ? byRank.get(it.next()) : endOfData());
      }
    };
  }

  public RowSortedTable<Integer, Witness, Set<Token>> asTable() {
    final TreeBasedTable<Integer, Witness, Set<Token>> table = TreeBasedTable.create(Ordering.natural(), Witness.SIGIL_COMPARATOR);
    for (Map.Entry<VariantGraph.Vertex, Integer> rank : byVertex.entrySet()) {
      final int row = rank.getValue();
      for (Token token : rank.getKey().tokens(witnesses)) {
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

  public VariantGraph.Vertex[][] asArray() {
    final Set<Integer> ranks = byRank.keySet();
    final VariantGraph.Vertex[][] arr = new VariantGraph.Vertex[ranks.size()][];
    for (final Iterator<Integer> it = ranks.iterator(); it.hasNext(); ) {
      final Integer rank = it.next();
      final SortedSet<Vertex> vertices = byRank.get(rank);
      arr[rank] = vertices.toArray(new Vertex[vertices.size()]);
    }
    return arr;
  }

  @Override
  public Integer apply(VariantGraph.Vertex vertex) {
    return byVertex.get(vertex);
  }

  @Override
  public int compare(VariantGraph.Vertex o1, VariantGraph.Vertex o2) {
    final Integer o1Rank = byVertex.get(o1);
    final Integer o2Rank = byVertex.get(o2);

    Preconditions.checkState(o1Rank != null, o1);
    Preconditions.checkState(o2Rank != null, o2);

    return (o1Rank.intValue() - o2Rank.intValue());
  }
}
