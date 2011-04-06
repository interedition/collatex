package eu.interedition.collatex2.experimental;

import java.util.Map;

import com.google.common.collect.Maps;

import eu.interedition.collatex2.implementation.containers.graph.VariantGraphEdge;
import eu.interedition.collatex2.implementation.containers.graph.VariantGraphVertex;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IVariantGraph;
import eu.interedition.collatex2.interfaces.IVariantGraphEdge;
import eu.interedition.collatex2.interfaces.IVariantGraphVertex;
import eu.interedition.collatex2.interfaces.IWitness;

public class MyNewAligner {

  private final IVariantGraph graph;

  public MyNewAligner(IVariantGraph graph) {
    this.graph = graph;
  }

  public void addWitness(IWitness witness) {
    Map<INormalizedToken, INormalizedToken> linkedTokens;
    IWitness the_witness;
    if (graph.isEmpty()) {
      linkedTokens = Maps.newLinkedHashMap();
      the_witness = witness;
    } else {
      //TODO: make superwitness here!
      IWitness a = graph.getWitnesses().get(0);
      MyNewLinker linker = new MyNewLinker();
      linkedTokens = linker.link(a, witness);
      the_witness = witness;
    }
    
    IVariantGraphVertex previous =  graph.getStartVertex();
    for (INormalizedToken token : the_witness.getTokens()) {
      // determine whether this token is a match or not
      IVariantGraphVertex vertex = linkedTokens.containsKey(token) ? findVertex(linkedTokens.get(token)) : addNewVertex(token.getNormalized(), token);
      IVariantGraphEdge edge = linkedTokens.containsKey(token) ? graph.getEdge(previous, vertex) : addNewEdge(previous, vertex);
      vertex.addToken(the_witness, token);
      edge.addWitness(the_witness);
      previous = vertex;
    }
    IVariantGraphEdge edge = graph.getEdge(previous, graph.getEndVertex());
    if (edge == null) edge = addNewEdge(previous, graph.getEndVertex());
    edge.addWitness(the_witness);
  }

  //TODO: this method should be deleted after the superbase is reintroduced!
  private IVariantGraphVertex findVertex(INormalizedToken token) {
    for (IVariantGraphVertex vertex: graph.vertexSet()) {
      for (IWitness witness : vertex.getWitnesses()) {
        INormalizedToken other = vertex.getToken(witness);
        if (other == token) return vertex;
      }
    }
    throw new RuntimeException("THIS SHOULD NOT BE POSSIBLE!");
  }
  
  //write
  private IVariantGraphVertex addNewVertex(String normalized, INormalizedToken vertexKey) {
    IVariantGraphVertex vertex = new VariantGraphVertex(normalized, vertexKey);
    graph.addVertex(vertex);
    return vertex;
  }

  //write
  private IVariantGraphEdge addNewEdge(IVariantGraphVertex begin, IVariantGraphVertex end) {
    // System.out.println("Add edge between "+begin.getNormalized()+ " and " + end.getNormalized());
    IVariantGraphEdge edge = new VariantGraphEdge();
    graph.addEdge(begin, end, edge);
    return edge;
  }


}
