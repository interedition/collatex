package eu.interedition.collatex2.experimental.graph;

import java.util.Iterator;
import java.util.List;

import org.jgrapht.DirectedGraph;

import eu.interedition.collatex2.interfaces.IWitness;

public interface IVariantGraph extends DirectedGraph<IVariantGraphVertex, IVariantGraphEdge> {

  IVariantGraphVertex getStartVertex();

  IVariantGraphVertex getEndVertex();

  // Iterates over vertices in topological order
  Iterator<IVariantGraphVertex> iterator();

  //NOTE: could extract Indexable interface!
  List<String> findRepeatingTokens();

  List<IWitness> getWitnesses();

  boolean isEmpty();

  //TODO: delete method? add edges based method to interface
  List<IVariantGraphVertex> getPath(IWitness witness);

  List<IVariantGraphVertex> getLongestPath();

}