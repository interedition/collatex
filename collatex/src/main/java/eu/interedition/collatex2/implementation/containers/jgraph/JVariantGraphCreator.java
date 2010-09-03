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

  public static IJVariantGraph parallelSegmentate(IVariantGraph unjoinedGraph) {
    IJVariantGraph joinedGraph = JVariantGraph.create();
    Set<IVariantGraphVertex> processedVertices = Sets.newHashSet();

    IVariantGraphVertex startVertex = unjoinedGraph.getStartVertex();
    IVariantGraphVertex endVertex = unjoinedGraph.getEndVertex();

    IJVariantGraphVertex lastJoinedVertex = joinedGraph.getStartVertex();

    growJoinedGraph(unjoinedGraph, joinedGraph, processedVertices, startVertex, lastJoinedVertex);

    joinedGraph.addVertex(new JVariantGraphVertex(endVertex));

    return joinedGraph;
  }

  private static void growJoinedGraph(IVariantGraph unjoinedGraph, IJVariantGraph joinedGraph, Set<IVariantGraphVertex> processedVertices, IVariantGraphVertex startVertex,
      IJVariantGraphVertex lastJoinedVertex) {
    while (unjoinedGraph.outDegreeOf(startVertex) > 0) {
      LOG.info("startVertex={}", startVertex);
      Set<IVariantGraphEdge> outgoingEdges = unjoinedGraph.outgoingEdgesOf(startVertex);
      for (IVariantGraphEdge edge : outgoingEdges) {
        LOG.info("edge={}", edge);
        IVariantGraphVertex currentVertex = edge.getEndVertex();
        IJVariantGraphVertex currentJoinedVertex = new JVariantGraphVertex(currentVertex);
        if (unjoinedGraph.outDegreeOf(currentVertex) == 1) {
          IVariantGraphEdge outEdge = unjoinedGraph.outgoingEdgesOf(currentVertex).iterator().next();
          IVariantGraphVertex nextVertex = outEdge.getEndVertex();
          if (unjoinedGraph.inDegreeOf(nextVertex) == 1) {
            // join currentVertex with nextVertex
            currentJoinedVertex.addVariantGraphVertex(nextVertex);
            processedVertices.add(currentVertex);
          } else {
            currentVertex = nextVertex;
            addAndRecurse(unjoinedGraph, joinedGraph, processedVertices, lastJoinedVertex, currentVertex, currentJoinedVertex);
          }
        } else {
          addAndRecurse(unjoinedGraph, joinedGraph, processedVertices, lastJoinedVertex, currentVertex, currentJoinedVertex);
        }
        startVertex = currentVertex;
      }
    }
  }

  private static void addAndRecurse(IVariantGraph unjoinedGraph, IJVariantGraph joinedGraph, Set<IVariantGraphVertex> processedVertices, IJVariantGraphVertex lastJoinedVertex,
      IVariantGraphVertex currentVertex, IJVariantGraphVertex currentJoinedVertex) {
    joinedGraph.addVertex(currentJoinedVertex);
    joinedGraph.addEdge(lastJoinedVertex, currentJoinedVertex);
    growJoinedGraph(unjoinedGraph, joinedGraph, processedVertices, currentVertex, lastJoinedVertex);
  }

}
