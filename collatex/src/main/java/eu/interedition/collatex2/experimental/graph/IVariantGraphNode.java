package eu.interedition.collatex2.experimental.graph;

import java.util.List;

import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IWitness;

public interface IVariantGraphNode {

  String getNormalized();

  INormalizedToken getToken();
  
  List<IVariantGraphArc> getArcs();

  void addNewArc(IVariantGraphNode node, IWitness witness, INormalizedToken token);

  boolean arcExist(IVariantGraphNode end);

  IVariantGraphArc find(IVariantGraphNode end);
  
  //TODO: add test!
  IVariantGraphArc findArc(IWitness witness);

  //TODO: add test!
  boolean hasArc(IWitness witness);



}
