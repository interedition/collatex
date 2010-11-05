package eu.interedition.collatex2.implementation.cgraph;

import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import eu.interedition.collatex2.implementation.containers.graph.CyclicVariantGraph;
import eu.interedition.collatex2.implementation.containers.graph.VariantGraphEdge;
import eu.interedition.collatex2.implementation.containers.graph.VariantGraphVertex;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IVariantGraph;
import eu.interedition.collatex2.interfaces.IVariantGraphEdge;
import eu.interedition.collatex2.interfaces.IVariantGraphVertex;

public class CVariantGraphCreator {
  private static final Logger LOG = LoggerFactory.getLogger(CVariantGraphCreator.class);
  private static IVariantGraph cyclicGraph;
  private static Set<IVariantGraphVertex> avgVerticesCopiedInCyclicVariantGraph = Sets.newHashSet();
  private static final Map<INormalizedToken, IVariantGraphVertex> keyToken2Vertex = Maps.newHashMap();

  public static IVariantGraph getCyclicVariantGraph(final IVariantGraph acyclicGraph) {
    cyclicGraph = CyclicVariantGraph.create();

    IVariantGraphVertex aStartVertex = acyclicGraph.getStartVertex();
    IVariantGraphVertex cStartVertex = cyclicGraph.getStartVertex();

    avgVerticesCopiedInCyclicVariantGraph.add(aStartVertex);
    avgVerticesCopiedInCyclicVariantGraph.add(acyclicGraph.getEndVertex());
    processVertex(acyclicGraph, aStartVertex, cStartVertex);

    return cyclicGraph;
  }

  private static void processVertex(final IVariantGraph acyclicGraph, IVariantGraphVertex avgVertex, IVariantGraphVertex cvgVertex) {
    Set<IVariantGraphEdge> outgoingEdges = acyclicGraph.outgoingEdgesOf(avgVertex);
    for (IVariantGraphEdge avgEdge : outgoingEdges) {
      processEdge(avgEdge, acyclicGraph, cvgVertex);
    }
  }

  private static void processEdge(IVariantGraphEdge avgEdge, final IVariantGraph acyclicGraph, IVariantGraphVertex cvgLastVertex) {
    IVariantGraphVertex avgTargetVertex = acyclicGraph.getEdgeTarget(avgEdge);
    INormalizedToken vertexKey = avgTargetVertex.getVertexKey();
    IVariantGraphVertex cvgTargetVertex;
    if (keyToken2Vertex.containsKey(vertexKey)) {
      cvgTargetVertex = keyToken2Vertex.get(vertexKey);
    } else {
      cvgTargetVertex = new VariantGraphVertex(avgTargetVertex.getNormalized(), null);
      keyToken2Vertex.put(vertexKey, cvgTargetVertex);
      cyclicGraph.addVertex(cvgTargetVertex);
    }
    VariantGraphEdge cvgEdge = new VariantGraphEdge(cvgLastVertex, cvgTargetVertex, avgTargetVertex.getWitnesses().iterator().next());
    cyclicGraph.addEdge(cvgLastVertex, cvgTargetVertex, cvgEdge);
    if (!avgVerticesCopiedInCyclicVariantGraph.contains(avgTargetVertex)) {
      processVertex(acyclicGraph, avgTargetVertex, cvgTargetVertex);
    }
  }
}
