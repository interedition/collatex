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

package eu.interedition.collatex2.implementation.graph.ranked;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import eu.interedition.collatex2.implementation.graph.segmented.ISegmentedVariantGraph;
import eu.interedition.collatex2.implementation.graph.segmented.ISegmentedVariantGraphVertex;
import eu.interedition.collatex2.interfaces.IVariantGraphEdge;

public class VariantGraphRanker {

  private final ISegmentedVariantGraph graph;

  public VariantGraphRanker(ISegmentedVariantGraph graph) {
    this.graph = graph;
  }

  public Iterator<IRankedVariantGraphVertex> iterator() {
    final Map<ISegmentedVariantGraphVertex, Integer> vertexToRankMap = Maps.newLinkedHashMap();
    final Iterator<ISegmentedVariantGraphVertex> iterator = graph.iterator();
    ISegmentedVariantGraphVertex startVertex = iterator.next();
    vertexToRankMap.put(startVertex, 0);
    return new Iterator<IRankedVariantGraphVertex>() {

      //TODO: skip end vertex?
      @Override
      public boolean hasNext() {
        return iterator.hasNext();
      }

      @Override
      public IRankedVariantGraphVertex next() {
        ISegmentedVariantGraphVertex vertex = iterator.next();
        Set<IVariantGraphEdge> incomingEdges = graph.incomingEdgesOf(vertex);
        int maxRankParent = -1;
        for (IVariantGraphEdge edgeFromParent : incomingEdges) {
          ISegmentedVariantGraphVertex parent = graph.getEdgeSource(edgeFromParent);
          maxRankParent = Math.max(maxRankParent, vertexToRankMap.get(parent));
        }
        int rank = maxRankParent+1;
        vertexToRankMap.put(vertex, rank);
        return new RankedVariantGraphVertex(rank, vertex);
      }

      @Override
      public void remove() {
        iterator.remove();
      }};
  }

  public List<IRankedVariantGraphVertex> getRankedVertices() {
    return Lists.newArrayList(iterator());
  }

}
