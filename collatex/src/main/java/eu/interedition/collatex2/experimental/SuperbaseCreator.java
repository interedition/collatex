package eu.interedition.collatex2.experimental;

import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Lists;

import eu.interedition.collatex2.implementation.containers.witness.Witness;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IVariantGraph;
import eu.interedition.collatex2.interfaces.IVariantGraphVertex;
import eu.interedition.collatex2.interfaces.IWitness;

public class SuperbaseCreator {

  public IWitness create(IVariantGraph graph) {
    List<INormalizedToken> superbaseTokens = Lists.newArrayList();
    Iterator<IVariantGraphVertex> iterator = graph.iterator();
    while (iterator.hasNext()) {
      IVariantGraphVertex vertex = iterator.next();
      if (vertex != graph.getStartVertex() && vertex != graph.getEndVertex()) {
        superbaseTokens.add(vertex);
      }
    }
    return new Witness("superbase", superbaseTokens);
  }
}
