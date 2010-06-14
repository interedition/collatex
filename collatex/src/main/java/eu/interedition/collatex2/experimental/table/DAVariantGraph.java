package eu.interedition.collatex2.experimental.table;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jgrapht.alg.BellmanFordShortestPath;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph;

import com.google.common.collect.Lists;

import eu.interedition.collatex2.interfaces.IWitness;

@SuppressWarnings("serial")
public class DAVariantGraph extends DirectedAcyclicGraph<CollateXVertex, CollateXEdge> {

  public DAVariantGraph(Class<? extends CollateXEdge> arg0) {
    super(arg0);
  }

  public List<CollateXVertex> getLongestPath() {
    // NOTE: Weights are set to negative value to
    // generate the longest path instead of the shortest path
    for (CollateXEdge edge : edgeSet()) {
      setEdgeWeight(edge, -1);
    }
    // NOTE: gets the start vertex of the graph
    CollateXVertex startVertex = getStartVertex();
    CollateXVertex endVertex = getEndVertex();
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

  //NOTE: not the nicest code in the world
  private CollateXVertex getEndVertex() {
    Iterator<CollateXVertex> i = iterator();
    CollateXVertex end = null;
    while(i.hasNext()) {
      end = i.next();
    }
    return end;
  }

  private CollateXVertex getStartVertex() {
    return iterator().next();
  }

  List<CollateXVertex> getPathFor(IWitness witness) {
    List<CollateXVertex> path = Lists.newArrayList();
    CollateXVertex startVertex = iterator().next();
    CollateXVertex currentVertex = startVertex;
    while (outDegreeOf(currentVertex) > 0) {
      Set<CollateXEdge> outgoingEdges = outgoingEdgesOf(currentVertex);
      for (CollateXEdge edge : outgoingEdges) {
        //TODO: looking at the vertex here is wrong!
        CollateXVertex edgeTarget = getEdgeTarget(edge);
        if (edgeTarget.containsWitness(witness.getSigil())) {
          if (!edgeTarget.getNormalized().equals("#")) {
            path.add(edgeTarget);
          }
        }
        currentVertex = edgeTarget;
      }
    }
    return path;
  }
}
