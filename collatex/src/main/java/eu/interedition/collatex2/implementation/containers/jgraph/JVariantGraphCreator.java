package eu.interedition.collatex2.implementation.containers.jgraph;

import java.util.Set;

import com.google.common.collect.Sets;

import eu.interedition.collatex2.interfaces.IJVariantGraph;
import eu.interedition.collatex2.interfaces.IVariantGraph;
import eu.interedition.collatex2.interfaces.IVariantGraphEdge;
import eu.interedition.collatex2.interfaces.IVariantGraphVertex;

public class JVariantGraphCreator {
  public static IJVariantGraph parallelSegmentate(IVariantGraph inGraph) {
    IJVariantGraph outGraph = JVariantGraph.create();
    Set<IVariantGraphVertex> processedVertices = Sets.newHashSet();

    // start with the startVertex
    IVariantGraphVertex startVertex = inGraph.getStartVertex();
    outGraph.addVertex(new JVariantVertex(startVertex));
    IVariantGraphVertex endVertex = inGraph.getEndVertex();

    while (!startVertex.equals(endVertex)) {
      Set<IVariantGraphEdge> outgoingEdges = inGraph.outgoingEdgesOf(startVertex);
      for (IVariantGraphEdge edge : outgoingEdges) {
        IVariantGraphVertex currentInVertex = edge.getEndVertex();
        if (inGraph.outDegreeOf(currentInVertex) == 1) {
          IVariantGraphEdge outEdge = inGraph.outgoingEdgesOf(currentInVertex).iterator().next();
          IVariantGraphVertex nextVertex = outEdge.getEndVertex();
          if (inGraph.inDegreeOf(nextVertex) == 1) {
            // join currentVertex with nextVertex

            processedVertices.add(currentInVertex);
          } else {
            // end join
          }
        } else {
          // end join!
        }
      }
    }

    outGraph.addVertex(new JVariantVertex(endVertex));

    return outGraph;
  }

}
