package eu.interedition.collatex2.interfaces;

import java.util.Set;


public interface IVariantGraphVertex extends INormalizedToken {

  String getNormalized();

  INormalizedToken getToken(IWitness witness);

  void addToken(IWitness witness, INormalizedToken token);

  Set<IWitness> getWitnesses();

  IWitness getWitnessForSigil(String sigil);

  boolean containsWitness(String sigil);

}
