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
import eu.interedition.collatex.implementation.graph.VariantGraphEdge;
import eu.interedition.collatex.implementation.graph.joined.JoinedVariantGraph;
import eu.interedition.collatex.implementation.graph.joined.JoinedVariantGraphEdge;
import eu.interedition.collatex.implementation.graph.joined.JoinedVariantGraphVertex;
import eu.interedition.collatex.interfaces.INormalizedToken;
import eu.interedition.collatex.interfaces.IVariantGraphEdge;
import eu.interedition.collatex.interfaces.IVariantGraphVertex;
import eu.interedition.collatex.interfaces.IWitness;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class JGraphToSegmentedVariantGraphConverter {

  public ISegmentedVariantGraph convert(JoinedVariantGraph joinedVariantGraph) {
    Set<JoinedVariantGraphVertex> vertexSet = joinedVariantGraph.vertexSet();
    Map<JoinedVariantGraphVertex, ISegmentedVariantGraphVertex> newVertices = createNewVertices(vertexSet);
    SegmentedVariantGraph segmentedVariantGraph = new SegmentedVariantGraph();
    for (ISegmentedVariantGraphVertex vertex : newVertices.values()) {
      segmentedVariantGraph.addVertex(vertex);
    }
    addEdgesToGraph(joinedVariantGraph, newVertices, segmentedVariantGraph);
    // set new end vertex
    ISegmentedVariantGraphVertex newEndVertex = newVertices.get(joinedVariantGraph.getEnd());
    segmentedVariantGraph.setEndVertex(newEndVertex);
    return segmentedVariantGraph;
  }

  private void addEdgesToGraph(JoinedVariantGraph joinedVariantGraph,
      Map<JoinedVariantGraphVertex, ISegmentedVariantGraphVertex> newVertices,
      ISegmentedVariantGraph segmentedVariantGraph) {
    Set<JoinedVariantGraphEdge> edgeSet = joinedVariantGraph.edgeSet();
    for (JoinedVariantGraphEdge edge : edgeSet) {
      JoinedVariantGraphVertex edgeSource = joinedVariantGraph.getEdgeSource(edge);
      JoinedVariantGraphVertex edgeTarget = joinedVariantGraph.getEdgeTarget(edge);
      IVariantGraphEdge newEdge = new VariantGraphEdge();
      for (IWitness witness : edge.getWitnesses()) {
        newEdge.addWitness(witness);
      }
      ISegmentedVariantGraphVertex startVertex = newVertices.get(edgeSource);
      ISegmentedVariantGraphVertex endVertex = newVertices.get(edgeTarget);
      segmentedVariantGraph.addEdge(startVertex, endVertex);
    }
  }

  private Map<JoinedVariantGraphVertex, ISegmentedVariantGraphVertex> createNewVertices(Set<JoinedVariantGraphVertex> vertexSet) {
    Map<JoinedVariantGraphVertex, ISegmentedVariantGraphVertex> newVertices = Maps.newLinkedHashMap();
    for (JoinedVariantGraphVertex vertex : vertexSet) {
      List<IVariantGraphVertex> vertices = vertex.getSources();
      Set<IWitness> witnesses = vertex.getWitnesses();
      Map<IWitness, List<INormalizedToken>> phraseForEachWitness = Maps.newLinkedHashMap();
      for (IWitness witness : witnesses) {
        List<INormalizedToken> tokensForThisWitness = Lists.newArrayList();
        for (IVariantGraphVertex vgVertex : vertices) {
          INormalizedToken token = vgVertex.getToken(witness);
          tokensForThisWitness.add(token);
        }
        phraseForEachWitness.put(witness, tokensForThisWitness);
      }
      newVertices.put(vertex, new SegmentedVariantGraphVertex(phraseForEachWitness));
    }
    return newVertices;
  }
}
