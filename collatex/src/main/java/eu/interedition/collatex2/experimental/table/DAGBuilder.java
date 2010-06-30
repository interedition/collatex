package eu.interedition.collatex2.experimental.table;

import java.util.Map;
import java.util.Set;

import org.jgrapht.alg.CycleDetector;

import com.google.common.collect.Maps;

import eu.interedition.collatex2.experimental.graph.IVariantGraph;
import eu.interedition.collatex2.experimental.graph.IVariantGraphEdge;
import eu.interedition.collatex2.experimental.graph.IVariantGraphVertex;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IWitness;

public class DAGBuilder {

  public DAVariantGraph buildDAG(IVariantGraph graph) {
    System.out.println("Building DAG!");
    //detect cycles
    CycleDetector<IVariantGraphVertex, IVariantGraphEdge> detector = new CycleDetector<IVariantGraphVertex, IVariantGraphEdge>(graph);
    Set<IVariantGraphVertex> findCycles = detector.findCycles();
    for (IVariantGraphVertex vertex : findCycles) {
      System.out.println(vertex.getNormalized());
    }
    // bla
    DAVariantGraph dag = new DAVariantGraph(CollateXEdge.class);
    Set<IVariantGraphVertex> vertices = graph.vertexSet();
    Map<IVariantGraphVertex, CollateXVertex> map = Maps.newLinkedHashMap();
    // convert VariantGraph vertices to DAG vertices 
    for (IVariantGraphVertex vGVertex : vertices) {
      CollateXVertex vertex = new CollateXVertex(vGVertex.getNormalized());
      dag.addVertex(vertex);
      map.put(vGVertex, vertex);
    }
    // convert VariantGraph edges to DAG edges
    Set<IVariantGraphEdge> edges = graph.edgeSet();
    for (IVariantGraphEdge vGEdge : edges) {
      IVariantGraphVertex beginVertex = vGEdge.getBeginVertex();
      IVariantGraphVertex endVertex = vGEdge.getEndVertex();
      CollateXVertex source = map.get(beginVertex);
      CollateXVertex dest = map.get(endVertex);
      CollateXEdge edge = new CollateXEdge();
      dag.addEdge(source, dest, edge);
      // convert witnesses on VariantGraph edge to DAG edge
      for (IWitness w: vGEdge.getWitnesses()) {
        edge.addWitness(w);
      }
      // convert tokens for each witness
      if (endVertex != graph.getEndVertex()) {
        for (IWitness witness: vGEdge.getWitnesses()) {
          INormalizedToken token = endVertex.getToken(witness);
          dest.addToken(witness, token);
        }
      }
    }
    return dag;
  }
}
