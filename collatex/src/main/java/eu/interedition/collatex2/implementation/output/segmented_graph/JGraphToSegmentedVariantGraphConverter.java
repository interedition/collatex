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

package eu.interedition.collatex2.implementation.output.segmented_graph;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import eu.interedition.collatex2.implementation.containers.graph.VariantGraphEdge;
import eu.interedition.collatex2.implementation.input.Phrase;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IPhrase;
import eu.interedition.collatex2.interfaces.IVariantGraphEdge;
import eu.interedition.collatex2.interfaces.IVariantGraphVertex;
import eu.interedition.collatex2.interfaces.IWitness;
import eu.interedition.collatex2.interfaces.nonpublic.joined_graph.IJVariantGraph;
import eu.interedition.collatex2.interfaces.nonpublic.joined_graph.IJVariantGraphEdge;
import eu.interedition.collatex2.interfaces.nonpublic.joined_graph.IJVariantGraphVertex;

public class JGraphToSegmentedVariantGraphConverter {

  public ISegmentedVariantGraph convert(IJVariantGraph joinedVariantGraph) {
    Set<IJVariantGraphVertex> vertexSet = joinedVariantGraph.vertexSet();
    Map<IJVariantGraphVertex, ISegmentedVariantGraphVertex> newVertices = createNewVertices(vertexSet);
    SegmentedVariantGraph segmentedVariantGraph = new SegmentedVariantGraph();
    for (ISegmentedVariantGraphVertex vertex : newVertices.values()) {
      segmentedVariantGraph.addVertex(vertex);
    }
    addEdgesToGraph(joinedVariantGraph, newVertices, segmentedVariantGraph);
    // set new end vertex
    ISegmentedVariantGraphVertex newEndVertex = newVertices.get(joinedVariantGraph.getEndVertex());
    segmentedVariantGraph.setEndVertex(newEndVertex);
    return segmentedVariantGraph;
  }

  private void addEdgesToGraph(IJVariantGraph joinedVariantGraph,
      Map<IJVariantGraphVertex, ISegmentedVariantGraphVertex> newVertices,
      ISegmentedVariantGraph segmentedVariantGraph) {
    Set<IJVariantGraphEdge> edgeSet = joinedVariantGraph.edgeSet();
    for (IJVariantGraphEdge edge : edgeSet) {
      IJVariantGraphVertex edgeSource = joinedVariantGraph.getEdgeSource(edge);    
      IJVariantGraphVertex edgeTarget = joinedVariantGraph.getEdgeTarget(edge);
      IVariantGraphEdge newEdge = new VariantGraphEdge();
      for (IWitness witness : edge.getWitnesses()) {
        newEdge.addWitness(witness);
      }
      ISegmentedVariantGraphVertex startVertex = newVertices.get(edgeSource);
      ISegmentedVariantGraphVertex endVertex = newVertices.get(edgeTarget);
      segmentedVariantGraph.addEdge(startVertex, endVertex);
    }
  }

  private Map<IJVariantGraphVertex, ISegmentedVariantGraphVertex> createNewVertices(Set<IJVariantGraphVertex> vertexSet) {
    Map<IJVariantGraphVertex, ISegmentedVariantGraphVertex> newVertices = Maps.newLinkedHashMap();
    for (IJVariantGraphVertex vertex : vertexSet) {
      List<IVariantGraphVertex> vertices = vertex.getVariantGraphVertices();
      Set<IWitness> witnesses = vertex.getWitnesses();
      Map<IWitness, IPhrase> phraseForEachWitness = Maps.newLinkedHashMap();
      for (IWitness witness : witnesses) {
        List<INormalizedToken> tokensForThisWitness = Lists.newArrayList();
        for (IVariantGraphVertex vgVertex : vertices) {
          INormalizedToken token = vgVertex.getToken(witness);
          tokensForThisWitness.add(token);
        }
        Phrase phrase  = new Phrase(tokensForThisWitness);
        phraseForEachWitness.put(witness, phrase);
      }
      newVertices.put(vertex, new SegmentedVariantGraphVertex(phraseForEachWitness));
    }
    return newVertices;
  }
}
