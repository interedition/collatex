package eu.interedition.collatex2.interfaces;

import java.util.Iterator;
import java.util.List;

import org.jgrapht.DirectedGraph;


public interface IVariantGraph extends DirectedGraph<IVariantGraphVertex, IVariantGraphEdge>, ITokenContainer {

  IVariantGraphVertex getStartVertex();

  IVariantGraphVertex getEndVertex();

  // Iterates over vertices in topological order
  Iterator<IVariantGraphVertex> iterator();

  List<IWitness> getWitnesses();

  boolean isEmpty();

  //TODO: delete method? add edges based method to interface
  List<IVariantGraphVertex> getPath(IWitness witness);

  List<IVariantGraphVertex> getLongestPath();

}