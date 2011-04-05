package eu.interedition.collatex2.experimental;

import java.util.List;
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
    Map<INormalizedToken, IVariantGraphVertex> vertices = createVerticesForNonMatches(witness.getTokens());
    addWitnessTokensToVertices(witness, vertices);
    addEdges(witness, vertices);
  }

  private Map<INormalizedToken, IVariantGraphVertex> createVerticesForNonMatches(List<INormalizedToken> tokens) {
    Map<INormalizedToken, IVariantGraphVertex> newVertices = Maps.newLinkedHashMap();
    for (INormalizedToken token : tokens) {
      IVariantGraphVertex vertex = addNewVertex(token.getNormalized(), token);
      newVertices.put(token, vertex);
    }
    return newVertices;
  }

  private void addWitnessTokensToVertices(IWitness witness, Map<INormalizedToken, IVariantGraphVertex> vertices) {
    for (INormalizedToken token : witness.getTokens()) {
      IVariantGraphVertex vertex = vertices.get(token);
      vertex.addToken(witness, token);
    }
  }

  //TODO: make adding new edge optional!
  private void addEdges(IWitness witness, Map<INormalizedToken, IVariantGraphVertex> vertices) {
    IVariantGraphVertex previous = graph.getStartVertex();
    for (INormalizedToken token : witness.getTokens()) {
      IVariantGraphVertex vertex = vertices.get(token);
      addNewEdge(previous, vertex, witness);
      previous = vertex;
    }
    addNewEdge(previous, graph.getEndVertex(), witness);
  }

  
  //write
  private IVariantGraphVertex addNewVertex(String normalized, INormalizedToken vertexKey) {
    final VariantGraphVertex vertex = new VariantGraphVertex(normalized, vertexKey);
    graph.addVertex(vertex);
    return vertex;
  }

  //write
  private void addNewEdge(IVariantGraphVertex begin, IVariantGraphVertex end, IWitness witness) {
    IVariantGraphEdge edge = new VariantGraphEdge();
    edge.addWitness(witness);
    graph.addEdge(begin, end, edge);
  }


}
