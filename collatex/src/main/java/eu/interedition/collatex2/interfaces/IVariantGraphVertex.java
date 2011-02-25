package eu.interedition.collatex2.interfaces;

import java.util.Set;

public interface IVariantGraphVertex extends INormalizedToken {

  String getNormalized();

  INormalizedToken getToken(IWitness witness);

  boolean containsWitness(IWitness witness);

  Set<IWitness> getWitnesses();

  INormalizedToken getVertexKey();

  void addToken(IWitness witness, INormalizedToken token);

}
