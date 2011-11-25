/**
 * CollateX - a Java library for collating textual sources,
 * for example, to produce an apparatus.
 *
 * Copyright (C) 2010 ESF COST Action "Interedition".
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.interedition.collatex.implementation.graph;

import com.google.common.base.Preconditions;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import eu.interedition.collatex.interfaces.*;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph;

import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * Like a VariantGraph, only with parallel segmentation applied so that vertices contain phrases instead of tokens.
 * <p/>
 * This class has a strong relation with {@link JoinedVariantGraph}.
 * It might be a good idea to merge the two in the future.
 *
 * @author ronald
 */
@SuppressWarnings("serial")
public class SegmentedVariantGraph extends DirectedAcyclicGraph<SegmentedVariantGraphVertex, IVariantGraphEdge> {
  private SegmentedVariantGraphVertex end;

  public static SegmentedVariantGraph create(JoinedVariantGraph joined) {
    final Map<JoinedVariantGraphVertex, SegmentedVariantGraphVertex> vertexMap = Maps.newLinkedHashMap();

    for (JoinedVariantGraphVertex vertex : joined.vertexSet()) {
      final Map<IWitness, List<INormalizedToken>> phrases = Maps.newLinkedHashMap();
      for (IWitness witness : vertex.getWitnesses()) {
        final List<INormalizedToken> phrase = Lists.newArrayList();
        for (IVariantGraphVertex source : vertex.getSources()) {
          phrase.add(source.getToken(witness));
        }
        phrases.put(witness, phrase);
      }
      vertexMap.put(vertex, new SegmentedVariantGraphVertex(phrases));
    }

    final SegmentedVariantGraph segmentedVariantGraph = new SegmentedVariantGraph();
    for (SegmentedVariantGraphVertex vertex : vertexMap.values()) {
      segmentedVariantGraph.addVertex(vertex);
    }

    for (JoinedVariantGraphEdge edge : joined.edgeSet()) {
      // FIXME: newEdge is never added to the graph?
      final IVariantGraphEdge newEdge = new VariantGraphEdge();
      for (IWitness witness : edge.getWitnesses()) {
        newEdge.addWitness(witness);
      }
      final SegmentedVariantGraphVertex startVertex = vertexMap.get(joined.getEdgeSource(edge));
      final SegmentedVariantGraphVertex endVertex = vertexMap.get(joined.getEdgeTarget(edge));
      segmentedVariantGraph.addEdge(startVertex, endVertex);
    }

    segmentedVariantGraph.end = vertexMap.get(joined.getEnd());

    return segmentedVariantGraph;
  }

  public static SegmentedVariantGraph create(IVariantGraph graph) {
    final Map<IVariantGraphVertex, SegmentedVariantGraphVertex> vertixMap = Maps.newLinkedHashMap();
    for (IVariantGraphVertex vertex : graph.vertexSet()) {
      final Map<IWitness, List<INormalizedToken>> phrases = Maps.newLinkedHashMap();
      for (IWitness witness : vertex.getWitnesses()) {
        phrases.put(witness, Lists.newArrayList(vertex.getToken(witness)));
      }
      vertixMap.put(vertex, new SegmentedVariantGraphVertex(phrases));
    }

    final SegmentedVariantGraph segmentedGraph = new SegmentedVariantGraph();
    for (SegmentedVariantGraphVertex newVertex : vertixMap.values()) {
      segmentedGraph.addVertex(newVertex);
    }
    for (IVariantGraphEdge edge : graph.edgeSet()) {
      final SegmentedVariantGraphVertex sourceVertex = vertixMap.get(graph.getEdgeSource(edge));
      final SegmentedVariantGraphVertex targetVertex = vertixMap.get(graph.getEdgeTarget(edge));
      segmentedGraph.addEdge(sourceVertex, targetVertex, edge);
    }
    return segmentedGraph;
  }

  private SegmentedVariantGraph() {
    super(VariantGraphEdge.class);
  }

  public SegmentedVariantGraphVertex getEnd() {
    return end;
  }

  public Iterable<RankedVariantGraphVertex> getRankedVertices() {
    final Iterator<SegmentedVariantGraphVertex> iterator = iterator();
    Preconditions.checkState(iterator.hasNext(), "No start vertex");

    final Map<SegmentedVariantGraphVertex, Integer> vertexRanking = Maps.newLinkedHashMap();
    vertexRanking.put(iterator.next(), 0);

    return new Iterable<RankedVariantGraphVertex>() {
      @Override
      public Iterator<RankedVariantGraphVertex> iterator() {
        return new AbstractIterator<RankedVariantGraphVertex>() {
          @Override
          protected RankedVariantGraphVertex computeNext() {
            if (!iterator.hasNext()) {
              return endOfData();
            }

            final SegmentedVariantGraphVertex vertex = iterator.next();
            int parentRank = -1;
            for (IVariantGraphEdge incoming : incomingEdgesOf(vertex)) {
              parentRank = Math.max(parentRank, vertexRanking.get(getEdgeSource(incoming)));
            }

            final int rank = parentRank + 1;
            vertexRanking.put(vertex, rank);
            return new RankedVariantGraphVertex(rank, vertex);
          }
        };
      }
    };
  }

}
