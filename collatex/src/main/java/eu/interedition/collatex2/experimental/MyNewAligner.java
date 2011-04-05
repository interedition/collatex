package eu.interedition.collatex2.experimental;

import java.util.List;

import com.google.common.collect.Lists;

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

  public void addWitness(IWitness firstWitness) {
    List<IVariantGraphVertex> newVertices = Lists.newArrayList();
    for (INormalizedToken token : firstWitness.getTokens()) {
      final IVariantGraphVertex vertex = addNewVertex(token.getNormalized(), token);
      vertex.addToken(firstWitness, token);
      newVertices.add(vertex);
    }
    IVariantGraphVertex previous = graph.getStartVertex();
    for (IVariantGraphVertex vertex : newVertices) {
      addNewEdge(previous, vertex, firstWitness);
      previous = vertex;
    }
    addNewEdge(previous, graph.getEndVertex(), firstWitness);
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
