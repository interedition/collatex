package eu.interedition.collatex2.implementation.output.segmented_graph;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import eu.interedition.collatex2.implementation.containers.graph.VariantGraphEdge;
import eu.interedition.collatex2.implementation.input.Phrase;
import eu.interedition.collatex2.interfaces.IJVariantGraph;
import eu.interedition.collatex2.interfaces.IJVariantGraphEdge;
import eu.interedition.collatex2.interfaces.IJVariantGraphVertex;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IPhrase;
import eu.interedition.collatex2.interfaces.IVariantGraphEdge;
import eu.interedition.collatex2.interfaces.IVariantGraphVertex;
import eu.interedition.collatex2.interfaces.IWitness;

public class JGraphToSegmentedVariantGraphConverter {

  public ISegmentedVariantGraph convert(IJVariantGraph joinedVariantGraph) {
    Set<IJVariantGraphVertex> vertexSet = joinedVariantGraph.vertexSet();
    Map<IJVariantGraphVertex, ISegmentedVariantGraphVertex> newVertices = createNewVertices(vertexSet);
    ISegmentedVariantGraph segmentedVariantGraph = new SegmentedVariantGraph();
    for (ISegmentedVariantGraphVertex vertex : newVertices.values()) {
      segmentedVariantGraph.addVertex(vertex);
    }
    addEdgesToGraph(joinedVariantGraph, newVertices, segmentedVariantGraph);
    return segmentedVariantGraph;
  }

  private void addEdgesToGraph(IJVariantGraph joinedVariantGraph,
      Map<IJVariantGraphVertex, ISegmentedVariantGraphVertex> newVertices,
      ISegmentedVariantGraph segmentedVariantGraph) {
    Set<IJVariantGraphEdge> edgeSet = joinedVariantGraph.edgeSet();
    for (IJVariantGraphEdge edge : edgeSet) {
      IJVariantGraphVertex edgeSource = joinedVariantGraph.getEdgeSource(edge);    
      IJVariantGraphVertex edgeTarget = joinedVariantGraph.getEdgeTarget(edge);
      //TODO: aargh: I dont like this default constructor at all!
      IVariantGraphEdge newEdge = new VariantGraphEdge(null, null, null);
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
