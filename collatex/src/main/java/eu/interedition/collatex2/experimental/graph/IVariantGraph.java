package eu.interedition.collatex2.experimental.graph;

import java.util.List;

import eu.interedition.collatex2.interfaces.IWitness;

public interface IVariantGraph {

  List<IVariantGraphNode> getNodes();

  IVariantGraphNode getStartNode();

  //NOTE: could move getArcs to the begin node of the arc!
  List<IVariantGraphArc> getArcs();

  //NOTE: could extract Indexable interface!
  //TODO: implement!
  List<String>  findRepeatingTokens();

  //TODO: add test!
  List<IWitness> getWitnesses();

  //TODO: add test!
  boolean isEmpty();

  //TODO: add test!
  IVariantGraphArc findArc(IVariantGraphNode begin, IWitness witness);

  //TODO: add test!
  boolean hasArc(IVariantGraphNode beginNode, IWitness first);

}