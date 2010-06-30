package eu.interedition.collatex2.experimental.graph;

import java.util.List;

import org.jgrapht.DirectedGraph;

import eu.interedition.collatex2.interfaces.IWitness;

public interface IVariantGraph extends DirectedGraph<IVariantGraphVertex, IVariantGraphEdge> {

//  List<IVariantGraphVertex> getVertices();

  //NOTE: This method is only here for testing purposes!
  List<IVariantGraphEdge> getEdges();

  IVariantGraphVertex getStartVertex();

  IVariantGraphVertex getEndVertex();

  //NOTE: could extract Indexable interface!
  //TODO: implement!
  List<String> findRepeatingTokens();

  //TODO: add test!
  List<IWitness> getWitnesses();

  //TODO: add test!
  boolean isEmpty();

  //TODO: delete method? add edges based method to interface
  //TODO: add test!
  List<IVariantGraphVertex> getPath(IWitness witness);

  void addWitness(IWitness a);

}