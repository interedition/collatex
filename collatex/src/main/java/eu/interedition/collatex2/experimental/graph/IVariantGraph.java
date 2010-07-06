package eu.interedition.collatex2.experimental.graph;

import java.util.Iterator;
import java.util.List;

import org.jgrapht.DirectedGraph;

import eu.interedition.collatex2.interfaces.IWitness;

public interface IVariantGraph extends DirectedGraph<IVariantGraphVertex, IVariantGraphEdge> {

  //TODO: remove this method --> use edgeSet() method instead
  List<IVariantGraphEdge> getEdges();

  IVariantGraphVertex getStartVertex();

  IVariantGraphVertex getEndVertex();

  // Iterates over vertices in topological order
  Iterator<IVariantGraphVertex> iterator();

  //NOTE: could extract Indexable interface!
  List<String> findRepeatingTokens();

  void addWitness(IWitness a);

  List<IWitness> getWitnesses();

  boolean isEmpty();

  //TODO: delete method? add edges based method to interface
  //TODO: add test!
  List<IVariantGraphVertex> getPath(IWitness witness);

  List<IVariantGraphVertex> getLongestPath();


}