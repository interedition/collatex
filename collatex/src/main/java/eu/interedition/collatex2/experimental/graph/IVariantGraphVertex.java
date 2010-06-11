package eu.interedition.collatex2.experimental.graph;

import java.util.List;
import java.util.Set;

import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IWitness;

public interface IVariantGraphVertex {

  String getNormalized();

  INormalizedToken getToken(IWitness witness);

  void addToken(IWitness witness, INormalizedToken token);

  //TODO: move back to graph?
  List<IVariantGraphEdge> getEdges();

  //TODO: move back to graph?
  void addNewEdge(IVariantGraphVertex node, IWitness witness);

  //TODO: move back to graph?
  IVariantGraphEdge findEdge(IVariantGraphVertex end);
  
  //TODO: move back to graph?
  //TODO: add test!
  IVariantGraphEdge findEdge(IWitness witness);

  //TODO: move back to graph?
  //TODO: add test!
  boolean hasEdge(IWitness witness);

  //TODO: move back to graph?
  boolean hasEdge(IVariantGraphVertex end);

  Set<IWitness> getWitnesses();

}
