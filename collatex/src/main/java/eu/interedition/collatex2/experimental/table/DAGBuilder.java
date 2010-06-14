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
    List<IVariantGraphVertex> vertices = graph.getVertices();
    Map<IVariantGraphVertex, CollateXVertex> map = Maps.newLinkedHashMap();
    // convert VariantGraph vertices to DAG vertices 
    for (IVariantGraphVertex vGVertex : vertices) {
      CollateXVertex vertex = new CollateXVertex(vGVertex.getNormalized());
      dag.addVertex(vertex);
      map.put(vGVertex, vertex);
    }
    // convert VariantGraph edges to DAG edges
    for (IVariantGraphVertex vertex : vertices) {
      List<IVariantGraphEdge> vGEdges = vertex.getEdges();
      for (IVariantGraphEdge vGEdge : vGEdges) {
        IVariantGraphVertex endVertex = vGEdge.getEndVertex();
        CollateXVertex source = map.get(vertex);
        CollateXVertex dest = map.get(endVertex);
        CollateXEdge edge = new CollateXEdge();
        dag.addEdge(source, dest, edge);
        if (endVertex != graph.getEndVertex()) {
          // convert tokens for each witness
          for (IWitness witness: vGEdge.getWitnesses()) {
            INormalizedToken token = endVertex.getToken(witness);
            dest.addToken(witness, token);
          }
        }
      }
    }
    return dag;
  }
}
