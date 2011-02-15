package eu.interedition.collatex2.implementation.containers.graph;

import java.util.List;

import org.jgrapht.alg.BellmanFordShortestPath;

import com.google.common.collect.Lists;

import eu.interedition.collatex2.interfaces.IVariantGraphEdge;
import eu.interedition.collatex2.interfaces.IVariantGraphVertex;

public class VariantGraphUtil {
  private final VariantGraph2 graph;

  public VariantGraphUtil(VariantGraph2 graph) {
    this.graph = graph;
  }

  //TODO: should getLongestPath() method return IVariantGraphEdges?
  public List<IVariantGraphVertex> getLongestPath() {
    // NOTE: Weights are set to negative value to
    // generate the longest path instead of the shortest path
    for (IVariantGraphEdge edge : graph.edgeSet()) {
      graph.setEdgeWeight(edge, -1);
    }
    // NOTE: gets the start vertex of the graph
    IVariantGraphVertex startVertex = graph.getStartVertex();
    IVariantGraphVertex endVertex = graph.getEndVertex();
    // Note: calculates the longest path
    List<IVariantGraphEdge> findPathBetween = BellmanFordShortestPath.findPathBetween(graph, startVertex, endVertex);
    // Note: gets the end vertices associated with the edges of the path
    List<IVariantGraphVertex> vertices = Lists.newArrayList();
    for (IVariantGraphEdge edge : findPathBetween) {
      IVariantGraphVertex edgeTarget = graph.getEdgeTarget(edge);
      if (edgeTarget != endVertex) {
        vertices.add(edgeTarget);
      }
    }
    return vertices;
  }

}
