package eu.interedition.collatex2.experimental.graph;

import java.util.List;

import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IWitness;

public interface IVariantGraphVertex {

  String getNormalized();

  INormalizedToken getToken();
  
  List<IVariantGraphEdge> getEdges();

  void addNewEdge(IVariantGraphVertex node, IWitness witness, INormalizedToken token);

  IVariantGraphEdge findEdge(IVariantGraphVertex end);
  
  //TODO: add test!
  IVariantGraphEdge findEdge(IWitness witness);

  //TODO: add test!
  boolean hasEdge(IWitness witness);

  boolean hasEdge(IVariantGraphVertex end);

}
