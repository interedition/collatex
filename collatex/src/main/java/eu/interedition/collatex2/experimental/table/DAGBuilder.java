package eu.interedition.collatex2.experimental.table;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

import eu.interedition.collatex2.experimental.graph.IVariantGraphEdge;
import eu.interedition.collatex2.experimental.graph.IVariantGraphVertex;
import eu.interedition.collatex2.experimental.graph.VariantGraph;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IWitness;

public class DAGBuilder {

  public DAVariantGraph buildDAG(VariantGraph graph) {
    DAVariantGraph dag = new DAVariantGraph(CollateXEdge.class);
    List<IVariantGraphVertex> nodes = graph.getVertices();
    Map<IVariantGraphVertex, CollateXVertex> map = Maps.newLinkedHashMap();
    // convert nodes to vertices here
    for (IVariantGraphVertex node : nodes) {
      CollateXVertex vertex = new CollateXVertex(node.getNormalized());
      dag.addVertex(vertex);
      map.put(node, vertex);
    }
    // convert arcs to edges
    for (IVariantGraphVertex node : nodes) {
      List<IVariantGraphEdge> arcs = node.getEdges();
      for (IVariantGraphEdge arc : arcs) {
        IVariantGraphVertex endVertex = arc.getEndVertex();
        CollateXVertex source = map.get(node);
        CollateXVertex dest = map.get(endVertex);
        CollateXEdge edge = new CollateXEdge();
        dag.addEdge(source, dest, edge);
        // convert tokens for each witness
        for (IWitness witness: arc.getWitnesses()) {
          INormalizedToken token = endVertex.getToken(witness);
          dest.addToken(witness, token);
        }
      }
    }
    return dag;
  }
}
