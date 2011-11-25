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

package eu.interedition.collatex.implementation.graph.segmented;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import eu.interedition.collatex.interfaces.*;

import java.util.List;
import java.util.Map;

public class NonSegmentedGraphConverter {
  public ISegmentedVariantGraph convertGraph(IVariantGraph graph) {
    Map<IVariantGraphVertex, ISegmentedVariantGraphVertex> newVertices = mapVertices(graph);
    SegmentedVariantGraph segmentedGraph = createSegmentedGraph(newVertices);
    addEdges(graph, newVertices, segmentedGraph);
    return segmentedGraph;
  }

  private SegmentedVariantGraph createSegmentedGraph(Map<IVariantGraphVertex, ISegmentedVariantGraphVertex> newVertices) {
    SegmentedVariantGraph segmentedGraph = new SegmentedVariantGraph();
    for (ISegmentedVariantGraphVertex newVertex : newVertices.values()) {
      segmentedGraph.addVertex(newVertex);
    }
    return segmentedGraph;
  }

  private void addEdges(IVariantGraph graph, Map<IVariantGraphVertex, ISegmentedVariantGraphVertex> newVertices, SegmentedVariantGraph segmentedGraph) {
    for (IVariantGraphEdge edge : graph.edgeSet()) {
      IVariantGraphVertex edgeSource = graph.getEdgeSource(edge);    
      IVariantGraphVertex edgeTarget = graph.getEdgeTarget(edge);
      ISegmentedVariantGraphVertex sourceVertex = newVertices.get(edgeSource);
      ISegmentedVariantGraphVertex targetVertex = newVertices.get(edgeTarget);
      segmentedGraph.addEdge(sourceVertex, targetVertex, edge);
    }
  }

  private Map<IVariantGraphVertex, ISegmentedVariantGraphVertex> mapVertices(IVariantGraph graph) {
    Map<IVariantGraphVertex, ISegmentedVariantGraphVertex> newVertices = Maps.newLinkedHashMap();
    for (IVariantGraphVertex vertex : graph.vertexSet()) {
      //convert witnesses
      Map<IWitness, List<INormalizedToken>> phraseForEachWitness = Maps.newLinkedHashMap();
      for (IWitness witness : vertex.getWitnesses()) {
        phraseForEachWitness.put(witness, Lists.newArrayList(vertex.getToken(witness)));
      }
      SegmentedVariantGraphVertex newVertex = new SegmentedVariantGraphVertex(phraseForEachWitness);
      newVertices.put(vertex, newVertex);
    }
    return newVertices;
  }
}
