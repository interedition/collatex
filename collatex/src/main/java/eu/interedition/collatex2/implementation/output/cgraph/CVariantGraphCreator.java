package eu.interedition.collatex2.implementation.output.cgraph;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

import eu.interedition.collatex2.implementation.containers.graph.VariantGraphEdge;
import eu.interedition.collatex2.implementation.containers.graph.VariantGraphVertex;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IVariantGraph;
import eu.interedition.collatex2.interfaces.IVariantGraphEdge;
import eu.interedition.collatex2.interfaces.IVariantGraphVertex;
import eu.interedition.collatex2.interfaces.IWitness;

public class CVariantGraphCreator {
  private static final Logger LOG = LoggerFactory.getLogger(CVariantGraphCreator.class);
  private static IVariantGraph cyclicGraph;
  private static Map<INormalizedToken, IVariantGraphVertex> keyToken2Vertex;
  private static Map<IVariantGraphVertex, IVariantGraphVertex> a2cVertexMap;

  public static IVariantGraph getCyclicVariantGraph(final IVariantGraph acyclicGraph) {
    keyToken2Vertex = Maps.newHashMap();
    a2cVertexMap = Maps.newHashMap();
    cyclicGraph = CyclicVariantGraph.create();
    IVariantGraphVertex aStartVertex = acyclicGraph.getStartVertex();
    IVariantGraphVertex cStartVertex = cyclicGraph.getStartVertex();
    a2cVertexMap.put(aStartVertex, cStartVertex);
    a2cVertexMap.put(acyclicGraph.getEndVertex(), cyclicGraph.getEndVertex());
    processVertex(aStartVertex, acyclicGraph, cStartVertex);
    return cyclicGraph;
  }

  private static void processVertex(IVariantGraphVertex avgVertex, final IVariantGraph acyclicGraph, IVariantGraphVertex cvgVertex) {
    LOG.info("processVertex(avgVertex=({}),cvgVertex=({}))", new Object[] { avgVertex, cvgVertex });
    Set<IVariantGraphEdge> outgoingEdges = acyclicGraph.outgoingEdgesOf(avgVertex);
    for (IVariantGraphEdge avgEdge : outgoingEdges) {
      processEdge(avgEdge, acyclicGraph, cvgVertex);
    }
    //    LOG.info("cyclicGraph={}", cyclicGraph);
  }

  private static void processEdge(IVariantGraphEdge avgEdge, final IVariantGraph acyclicGraph, IVariantGraphVertex cvgLastVertex) {
    LOG.info("processEdge(avgEdge=({}),cvgLastVertex=({}))", new Object[] { avgEdge, cvgLastVertex });
    IVariantGraphVertex avgTargetVertex = acyclicGraph.getEdgeTarget(avgEdge);
    boolean avgTargetVertexIsNew = !a2cVertexMap.containsKey(avgTargetVertex);
    INormalizedToken vertexKey = avgTargetVertex.getVertexKey();
    LOG.info("vertexKey={}", vertexKey);
    IVariantGraphVertex cvgTargetVertex;
    if (keyToken2Vertex.containsKey(vertexKey)) {
      cvgTargetVertex = keyToken2Vertex.get(vertexKey);
    } else {
      cvgTargetVertex = new VariantGraphVertex(avgTargetVertex.getNormalized(), null);
      keyToken2Vertex.put(vertexKey, cvgTargetVertex);
      cyclicGraph.addVertex(cvgTargetVertex);
      a2cVertexMap.put(avgTargetVertex, cvgTargetVertex);
    }
    VariantGraphEdge cvgEdge = convertEdge(avgEdge);
    LOG.info("cvgLastVertex={}, cvgTargetVertex={}", cvgLastVertex, cvgTargetVertex);
    cyclicGraph.addEdge(cvgLastVertex, cvgTargetVertex, cvgEdge);
    if (avgTargetVertexIsNew) {
      processVertex(avgTargetVertex, acyclicGraph, cvgTargetVertex);
    }
    //    LOG.info("cyclicGraph={}", cyclicGraph);
  }

  private static VariantGraphEdge convertEdge(IVariantGraphEdge avgEdge) {
    IVariantGraphVertex cvgStart = a2cVertexMap.get(avgEdge.getBeginVertex());
    IVariantGraphVertex cvgEnd = a2cVertexMap.get(avgEdge.getEndVertex());
    Iterator<IWitness> witnessIterator = avgEdge.getWitnesses().iterator();
    if (cvgStart == null) {
      LOG.error("start=null for {}", avgEdge);
    }
    if (cvgEnd == null) {
      LOG.error("end=null for {}", avgEdge);
    }
    VariantGraphEdge cvgEdge = new VariantGraphEdge(cvgStart, cvgEnd, witnessIterator.next());
    while (witnessIterator.hasNext()) {
      cvgEdge.addWitness(witnessIterator.next());
    }
    return cvgEdge;
  }
}
