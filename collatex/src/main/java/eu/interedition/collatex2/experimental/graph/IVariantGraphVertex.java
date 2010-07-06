package eu.interedition.collatex2.experimental.graph;

import java.util.Set;

import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IWitness;

public interface IVariantGraphVertex {

  String getNormalized();

  INormalizedToken getToken(IWitness witness);

  void addToken(IWitness witness, INormalizedToken token);

  Set<IWitness> getWitnesses();

  IWitness getWitnessForSigil(String sigil);

  boolean containsWitness(String sigil);

}
