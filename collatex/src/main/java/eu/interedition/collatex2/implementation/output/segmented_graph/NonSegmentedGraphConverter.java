package eu.interedition.collatex2.implementation.output.segmented_graph;

import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import eu.interedition.collatex2.implementation.input.Phrase;
import eu.interedition.collatex2.interfaces.IPhrase;
import eu.interedition.collatex2.interfaces.IVariantGraph;
import eu.interedition.collatex2.interfaces.IVariantGraphEdge;
import eu.interedition.collatex2.interfaces.IVariantGraphVertex;
import eu.interedition.collatex2.interfaces.IWitness;

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
      Map<IWitness, IPhrase> phraseForEachWitness = Maps.newLinkedHashMap();
      for (IWitness witness : vertex.getWitnesses()) {
        phraseForEachWitness.put(witness, new Phrase(Lists.newArrayList(vertex.getToken(witness))));
      }
      SegmentedVariantGraphVertex newVertex = new SegmentedVariantGraphVertex(phraseForEachWitness);
      newVertices.put(vertex, newVertex);
    }
    return newVertices;
  }
}
