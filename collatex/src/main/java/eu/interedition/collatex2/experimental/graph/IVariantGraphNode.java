package eu.interedition.collatex2.experimental.graph;

import java.util.List;

import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IWitness;

public interface IVariantGraphNode {

  String getNormalized();

  INormalizedToken getToken();
  
  List<IVariantGraphEdge> getEdges();

  void addNewEdge(IVariantGraphNode node, IWitness witness, INormalizedToken token);

  IVariantGraphEdge findEdge(IVariantGraphNode end);
  
  //TODO: add test!
  IVariantGraphEdge findEdge(IWitness witness);

  //TODO: add test!
  boolean hasEdge(IWitness witness);

  boolean hasEdge(IVariantGraphNode end);

}
