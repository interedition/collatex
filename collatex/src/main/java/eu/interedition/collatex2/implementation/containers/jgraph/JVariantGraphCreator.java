package eu.interedition.collatex2.implementation.containers.jgraph;

import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

import eu.interedition.collatex2.interfaces.IJVariantGraph;
import eu.interedition.collatex2.interfaces.IJVariantGraphVertex;
import eu.interedition.collatex2.interfaces.IVariantGraph;
import eu.interedition.collatex2.interfaces.IVariantGraphEdge;
import eu.interedition.collatex2.interfaces.IVariantGraphVertex;

public class JVariantGraphCreator {
  private static final Logger LOG = LoggerFactory.getLogger(JVariantGraphCreator.class);
  private static IJVariantGraph joinedGraph;

  public static IJVariantGraph parallelSegmentate(final IVariantGraph unjoinedGraph) {
    joinedGraph = JVariantGraph.create();
    Map<IVariantGraphVertex, IJVariantGraphVertex> vertexMap = Maps.newHashMap();

    IVariantGraphVertex startVgVertex = unjoinedGraph.getStartVertex();
    JVariantGraphVertex startJvgVertex = new JVariantGraphVertex(startVgVertex);
    joinedGraph.setStartVertex(startJvgVertex);
    vertexMap.put(startVgVertex, startJvgVertex);

    IVariantGraphVertex endVgVertex = unjoinedGraph.getEndVertex();
    JVariantGraphVertex endJvgVertex = new JVariantGraphVertex(endVgVertex);
    joinedGraph.setEndVertex(endJvgVertex);
    vertexMap.put(endVgVertex, endJvgVertex);

    Set<IVariantGraphEdge> outgoingEdges = unjoinedGraph.outgoingEdgesOf(startVgVertex);
    for (IVariantGraphEdge vgEdge : outgoingEdges) {
      IVariantGraphVertex targetVgVertex = unjoinedGraph.getEdgeTarget(vgEdge);
      process(unjoinedGraph, vertexMap, targetVgVertex, startJvgVertex, vgEdge);
    }

    return joinedGraph;
  }

  private static void process(final IVariantGraph unjoinedGraph, final Map<IVariantGraphVertex, IJVariantGraphVertex> vertexMap, IVariantGraphVertex vgVertex, IJVariantGraphVertex lastJvgVertex,
      IVariantGraphEdge vgEdge) {
    IJVariantGraphVertex jvgVertex;
    if (vertexMap.containsKey(vgVertex)) {
      jvgVertex = vertexMap.get(vgVertex);
    } else {
      jvgVertex = new JVariantGraphVertex(vgVertex);
      vertexMap.put(vgVertex, jvgVertex);
      joinedGraph.addVertex(jvgVertex);
    }
    JVariantGraphEdge jvgEdge = new JVariantGraphEdge(lastJvgVertex, jvgVertex, vgEdge);
    joinedGraph.addEdge(lastJvgVertex, jvgVertex, jvgEdge);
    checkNextVertex(unjoinedGraph, vertexMap, vgVertex, jvgVertex);
  }

  private static void checkNextVertex(final IVariantGraph unjoinedGraph, final Map<IVariantGraphVertex, IJVariantGraphVertex> vertexMap, IVariantGraphVertex vgVertex, IJVariantGraphVertex jvgVertex) {
    Set<IVariantGraphEdge> outgoingEdges = unjoinedGraph.outgoingEdgesOf(vgVertex);
    if (outgoingEdges.size() == 1) {
      IVariantGraphVertex targetVgVertex = unjoinedGraph.getEdgeTarget(outgoingEdges.iterator().next());
      if (vertexHasOneIncomingEdge(unjoinedGraph, targetVgVertex) && vertexHasOutgoingEdges(unjoinedGraph, targetVgVertex)) {
        if (!vertexMap.containsKey(targetVgVertex)) {
          jvgVertex.addVariantGraphVertex(targetVgVertex);
        }
        vertexMap.put(targetVgVertex, jvgVertex);
        checkNextVertex(unjoinedGraph, vertexMap, targetVgVertex, jvgVertex);
      } else {
        process(unjoinedGraph, vertexMap, targetVgVertex, jvgVertex, outgoingEdges.iterator().next());
      }
    } else {
      for (IVariantGraphEdge vgEdge : outgoingEdges) {
        IVariantGraphVertex targetVgVertex = unjoinedGraph.getEdgeTarget(vgEdge);
        process(unjoinedGraph, vertexMap, targetVgVertex, jvgVertex, vgEdge);
      }
    }
  }

  private static boolean vertexHasOneIncomingEdge(final IVariantGraph unjoinedGraph, final IVariantGraphVertex nextVertex) {
    return unjoinedGraph.inDegreeOf(nextVertex) == 1;
  }

  private static boolean vertexHasOutgoingEdges(final IVariantGraph unjoinedGraph, IVariantGraphVertex startVertex) {
    return unjoinedGraph.outDegreeOf(startVertex) > 0;
  }

  //---------------------------//
  //  public static IJVariantGraph parallelSegmentate0(final IVariantGraph unjoinedGraph) {
  //    joinedGraph = JVariantGraph.create();
  //    final Set<IVariantGraphVertex> processedVertices = Sets.newHashSet();
  //    final IVariantGraphVertex startVertex = unjoinedGraph.getStartVertex();
  //    final IJVariantGraphVertex lastJoinedVertex = joinedGraph.getStartVertex();
  //    growJoinedGraph(unjoinedGraph, processedVertices, startVertex, lastJoinedVertex);
  //    return joinedGraph;
  //  }

  //  private static void growJoinedGraph(final IVariantGraph unjoinedGraph, final Set<IVariantGraphVertex> processedVertices, IVariantGraphVertex startVertex, final IJVariantGraphVertex lastJoinedVertex) {
  //    while (vertexHasOutgoingEdges(unjoinedGraph, startVertex)) {
  //      LOG.info("startVertex={}", startVertex);
  //      for (final IVariantGraphEdge edge : unjoinedGraph.outgoingEdgesOf(startVertex)) {
  //        LOG.info("edge={}", edge);
  //        IVariantGraphVertex currentVertex = edge.getEndVertex();
  //        if (!processedVertices.contains(currentVertex)) {
  //          final IJVariantGraphVertex currentJoinedVertex = new JVariantGraphVertex(currentVertex);
  //          //        joinedGraph.addVertex(currentJoinedVertex);
  //          currentVertex = recurse(unjoinedGraph, processedVertices, lastJoinedVertex, currentVertex, currentJoinedVertex);
  //        }
  //        startVertex = currentVertex;
  //      }
  //    }
  //  }

  //  private static IVariantGraphVertex recurse(final IVariantGraph unjoinedGraph, final Set<IVariantGraphVertex> processedVertices, final IJVariantGraphVertex lastJoinedVertex,
  //      IVariantGraphVertex currentVertex, final IJVariantGraphVertex currentJoinedVertex) {
  //    if (unjoinedGraph.outDegreeOf(currentVertex) == 1) {
  //      final IVariantGraphEdge outEdge = unjoinedGraph.outgoingEdgesOf(currentVertex).iterator().next();
  //      final IVariantGraphVertex nextVertex = outEdge.getEndVertex();
  //      if (vertexHasOneIncomingEdge(unjoinedGraph, nextVertex) && vertexHasOutgoingEdges(unjoinedGraph, nextVertex)) {
  //        // join currentVertex with nextVertex
  //        currentJoinedVertex.addVariantGraphVertex(nextVertex);
  //        LOG.info("currentJoinedVertex=" + currentJoinedVertex);
  //        processedVertices.add(currentVertex);
  //        if (!processedVertices.contains(nextVertex)) {
  //          recurse(unjoinedGraph, processedVertices, lastJoinedVertex, nextVertex, currentJoinedVertex);
  //        }
  //      } else {
  //        currentVertex = nextVertex;
  //        if (!processedVertices.contains(currentVertex)) {
  //          addAndRecurse(unjoinedGraph, processedVertices, lastJoinedVertex, currentVertex, currentJoinedVertex);
  //        }
  //      }
  //    } else {
  //      if (!processedVertices.contains(currentVertex)) {
  //        addAndRecurse(unjoinedGraph, processedVertices, lastJoinedVertex, currentVertex, currentJoinedVertex);
  //      }
  //    }
  //    return currentVertex;
  //  }

  //  private static void addAndRecurse(final IVariantGraph unjoinedGraph, final Set<IVariantGraphVertex> processedVertices, final IJVariantGraphVertex lastJoinedVertex,
  //      final IVariantGraphVertex currentVertex, final IJVariantGraphVertex currentJoinedVertex) {
  //    joinedGraph.addVertex(currentJoinedVertex);
  //    joinedGraph.addEdge(lastJoinedVertex, currentJoinedVertex, new JVariantGraphEdge(lastJoinedVertex, currentJoinedVertex, vgEdge));
  //    LOG.info("joinedGraph=" + joinedGraph);
  //    growJoinedGraph(unjoinedGraph, processedVertices, currentVertex, lastJoinedVertex);
  //  }

}
