package eu.interedition.collatex2.experimental.table;

import eu.interedition.collatex2.experimental.graph.IVariantGraph;

public class DAGBuilder {

  public IVariantGraph buildDAG(IVariantGraph graph) {
    return graph;
//    System.out.println("Building DAG!");
//    // bla
//    DAVariantGraph dag = new DAVariantGraph(CollateXEdge.class);
//    Set<IVariantGraphVertex> vertices = graph.vertexSet();
//    Map<IVariantGraphVertex, CollateXVertex> map = Maps.newLinkedHashMap();
//    // convert VariantGraph vertices to DAG vertices 
//    for (IVariantGraphVertex vGVertex : vertices) {
//      CollateXVertex vertex = new CollateXVertex(vGVertex.getNormalized());
//      dag.addVertex(vertex);
//      map.put(vGVertex, vertex);
//    }
//    // convert VariantGraph edges to DAG edges
//    Set<IVariantGraphEdge> edges = graph.edgeSet();
//    for (IVariantGraphEdge vGEdge : edges) {
//      IVariantGraphVertex beginVertex = vGEdge.getBeginVertex();
//      IVariantGraphVertex endVertex = vGEdge.getEndVertex();
//      CollateXVertex source = map.get(beginVertex);
//      CollateXVertex dest = map.get(endVertex);
//      CollateXEdge edge = new CollateXEdge();
//      dag.addEdge(source, dest, edge);
//      // convert witnesses on VariantGraph edge to DAG edge
//      for (IWitness w: vGEdge.getWitnesses()) {
//        edge.addWitness(w);
//      }
//      // convert tokens for each witness
//      if (endVertex != graph.getEndVertex()) {
//        for (IWitness witness: vGEdge.getWitnesses()) {
//          INormalizedToken token = endVertex.getToken(witness);
//          dest.addToken(witness, token);
//        }
//      }
//    }
//    return dag;
  }
}
