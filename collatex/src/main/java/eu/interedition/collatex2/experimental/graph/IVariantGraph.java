package eu.interedition.collatex2.experimental.graph;

import java.util.List;

import eu.interedition.collatex2.interfaces.IWitness;

public interface IVariantGraph {

  List<IVariantGraphNode> getNodes();

  IVariantGraphNode getStartNode();

  //NOTE: could extract Indexable interface!
  //TODO: implement!
  List<String>  findRepeatingTokens();

  //TODO: add test!
  List<IWitness> getWitnesses();

  //TODO: add test!
  boolean isEmpty();

  //TODO: delete method? add arcs based method to interface
  //TODO: add test!
  List<IVariantGraphNode> getPath(IWitness witness);

  //NOTE: This method is only here for testing purposes!
  List<IVariantGraphArc> getArcs();
}