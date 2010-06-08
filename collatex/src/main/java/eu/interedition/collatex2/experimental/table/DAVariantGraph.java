package eu.interedition.collatex2.experimental.table;

import java.util.Iterator;
import java.util.List;

import org.jgrapht.alg.BellmanFordShortestPath;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph;

import com.google.common.collect.Lists;

@SuppressWarnings("serial")
public class DAVariantGraph extends DirectedAcyclicGraph<CollateXVertex, CollateXEdge> {

  public DAVariantGraph(Class<? extends CollateXEdge> arg0) {
    super(arg0);
  }

  public List<CollateXVertex> getLongestPath() {
    // TODO: this is not right place for this code
    // TODO: move this code to the variant graph
    // NOTE: Generate end vertex
    final CollateXVertex endVertex = new CollateXVertex("#");
    this.addVertex(endVertex);
    // NOTE: connect all the end vertices of each witness to the end vertex of the graph
    Iterator<CollateXVertex> s = iterator();
    while (s.hasNext()) {
      CollateXVertex v = s.next();
      if (v != endVertex && outDegreeOf(v) == 0) {
        CollateXEdge e = new CollateXEdge();
        this.addEdge(v, endVertex, e);
      }
    }
    // NOTE: Weights are set to negative value to
    // generate the longest path instead of the shortest path
    for (CollateXEdge edge : edgeSet()) {
      setEdgeWeight(edge, -1);
    }
    // NOTE: gets the start vertex of the graph
    CollateXVertex startVertex = iterator().next();
    // Note: calculates the longest path
    List<CollateXEdge> findPathBetween = BellmanFordShortestPath.findPathBetween(this, startVertex, endVertex);
    // Note: gets the end vertices associated with the edges of the path
    List<CollateXVertex> vertices = Lists.newArrayList();
    for (CollateXEdge edge : findPathBetween) {
      CollateXVertex edgeTarget = this.getEdgeTarget(edge);
      if (edgeTarget != endVertex) {
        vertices.add(edgeTarget);
      }
    }
    return vertices;
  }
}
