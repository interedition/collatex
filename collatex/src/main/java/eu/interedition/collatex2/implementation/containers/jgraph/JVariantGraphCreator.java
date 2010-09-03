package eu.interedition.collatex2.implementation.containers.jgraph;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

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
    final Set<IVariantGraphVertex> processedVertices = Sets.newHashSet();
    final IVariantGraphVertex startVertex = unjoinedGraph.getStartVertex();
    final IJVariantGraphVertex lastJoinedVertex = joinedGraph.getStartVertex();
    growJoinedGraph(unjoinedGraph, processedVertices, startVertex, lastJoinedVertex);
    return joinedGraph;
  }

  private static void growJoinedGraph(final IVariantGraph unjoinedGraph, final Set<IVariantGraphVertex> processedVertices, IVariantGraphVertex startVertex, final IJVariantGraphVertex lastJoinedVertex) {
    while (vertexHasOutgoingEdges(unjoinedGraph, startVertex)) {
      LOG.info("startVertex={}", startVertex);
      final Set<IVariantGraphEdge> outgoingEdges = unjoinedGraph.outgoingEdgesOf(startVertex);
      for (final IVariantGraphEdge edge : outgoingEdges) {
        LOG.info("edge={}", edge);
        IVariantGraphVertex currentVertex = edge.getEndVertex();
        final IJVariantGraphVertex currentJoinedVertex = new JVariantGraphVertex(currentVertex);
        //        joinedGraph.addVertex(currentJoinedVertex);
        currentVertex = recurse(unjoinedGraph, processedVertices, lastJoinedVertex, currentVertex, currentJoinedVertex);
        startVertex = currentVertex;
      }
    }
  }

  private static boolean vertexHasOutgoingEdges(final IVariantGraph unjoinedGraph, IVariantGraphVertex startVertex) {
    return unjoinedGraph.outDegreeOf(startVertex) > 0;
  }

  private static IVariantGraphVertex recurse(final IVariantGraph unjoinedGraph, final Set<IVariantGraphVertex> processedVertices, final IJVariantGraphVertex lastJoinedVertex,
      IVariantGraphVertex currentVertex, final IJVariantGraphVertex currentJoinedVertex) {
    if (unjoinedGraph.outDegreeOf(currentVertex) == 1) {
      final IVariantGraphEdge outEdge = unjoinedGraph.outgoingEdgesOf(currentVertex).iterator().next();
      final IVariantGraphVertex nextVertex = outEdge.getEndVertex();
      if (vertexHasOneIncomingEdge(unjoinedGraph, nextVertex) && vertexHasOutgoingEdges(unjoinedGraph, nextVertex)) {
        // join currentVertex with nextVertex
        currentJoinedVertex.addVariantGraphVertex(nextVertex);
        LOG.info("currentJoinedVertex=" + currentJoinedVertex);
        processedVertices.add(currentVertex);
        currentVertex = recurse(unjoinedGraph, processedVertices, lastJoinedVertex, nextVertex, currentJoinedVertex);
      } else {
        currentVertex = nextVertex;
        addAndRecurse(unjoinedGraph, processedVertices, lastJoinedVertex, currentVertex, currentJoinedVertex);
      }
    } else {
      addAndRecurse(unjoinedGraph, processedVertices, lastJoinedVertex, currentVertex, currentJoinedVertex);
    }
    return currentVertex;
  }

  private static boolean vertexHasOneIncomingEdge(final IVariantGraph unjoinedGraph, final IVariantGraphVertex nextVertex) {
    return unjoinedGraph.inDegreeOf(nextVertex) == 1;
  }

  private static void addAndRecurse(final IVariantGraph unjoinedGraph, final Set<IVariantGraphVertex> processedVertices, final IJVariantGraphVertex lastJoinedVertex,
      final IVariantGraphVertex currentVertex, final IJVariantGraphVertex currentJoinedVertex) {
    joinedGraph.addVertex(currentJoinedVertex);
    joinedGraph.addEdge(lastJoinedVertex, currentJoinedVertex, new JVariantGraphEdge(lastJoinedVertex, currentJoinedVertex));
    LOG.info("joinedGraph=" + joinedGraph);
    growJoinedGraph(unjoinedGraph, processedVertices, currentVertex, lastJoinedVertex);
  }

}
