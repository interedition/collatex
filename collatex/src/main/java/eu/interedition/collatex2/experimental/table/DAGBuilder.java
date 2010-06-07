package eu.interedition.collatex2.experimental.table;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

import eu.interedition.collatex2.experimental.graph.IVariantGraphArc;
import eu.interedition.collatex2.experimental.graph.IVariantGraphNode;
import eu.interedition.collatex2.experimental.graph.VariantGraph;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IWitness;

public class DAGBuilder {

  public DAVariantGraph buildDAG(VariantGraph graph) {
    DAVariantGraph dag = new DAVariantGraph(CollateXEdge.class);
    List<IVariantGraphNode> nodes = graph.getNodes();
    Map<IVariantGraphNode, CollateXVertex> map = Maps.newLinkedHashMap();
    // convert nodes to vertices here
    for (IVariantGraphNode node : nodes) {
      CollateXVertex vertex = new CollateXVertex(node.getNormalized());
      dag.addVertex(vertex);
      map.put(node, vertex);
    }
    // convert arcs to edges
    for (IVariantGraphNode node : nodes) {
      List<IVariantGraphArc> arcs = node.getArcs();
      for (IVariantGraphArc arc : arcs) {
        IVariantGraphNode endNode = arc.getEndNode();
        CollateXVertex source = map.get(node);
        CollateXVertex dest = map.get(endNode);
        CollateXEdge edge = new CollateXEdge();
        dag.addEdge(source, dest, edge);
        // convert tokens for each witness
        for (IWitness witness: arc.getWitnesses()) {
          INormalizedToken token = arc.getToken(witness);
          dest.addToken(witness, token);
        }
      }
    }
    return dag;
  }
}
