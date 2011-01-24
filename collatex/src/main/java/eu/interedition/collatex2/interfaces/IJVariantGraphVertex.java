package eu.interedition.collatex2.interfaces;

import java.util.List;
import java.util.Set;

public interface IJVariantGraphVertex {
  String getNormalized();

  void addVariantGraphVertex(IVariantGraphVertex nextVertex);

  Set<IWitness> getWitnesses();

  List<IVariantGraphVertex> getVariantGraphVertices();
}
