package eu.interedition.collatex2.interfaces.nonpublic.joined_graph;

import java.util.List;
import java.util.Set;

import eu.interedition.collatex2.interfaces.IVariantGraphVertex;
import eu.interedition.collatex2.interfaces.IWitness;

public interface IJVariantGraphVertex {
  String getNormalized();

  void addVariantGraphVertex(IVariantGraphVertex nextVertex);

  Set<IWitness> getWitnesses();

  List<IVariantGraphVertex> getVariantGraphVertices();
}
