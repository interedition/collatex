package eu.interedition.collatex2.implementation.containers.graph;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;

import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IVariantGraphVertex;
import eu.interedition.collatex2.interfaces.IWitness;

public class VariantGraphVertex implements IVariantGraphVertex {
  private final String normalized;
  private final Map<IWitness, INormalizedToken> tokenMap;

  public VariantGraphVertex(String normalized) {
    this.normalized = normalized;
    this.tokenMap = Maps.newLinkedHashMap();
  }

  public String getNormalized() {
    return normalized;
  }

  public INormalizedToken getToken(IWitness witness) {
    if (!tokenMap.containsKey(witness)) {
      throw new RuntimeException("TOKEN FOR WITNESS " + witness.getSigil() + " NOT FOUND IN VERTEX " + getNormalized() + "!");
    }
    return tokenMap.get(witness);
  }

  public void addToken(IWitness witness, INormalizedToken token) {
    tokenMap.put(witness, token);
  }

  //TODO: change String parameter into IWitness
  public boolean containsWitness(String sigil) {
    return (internalGetWitnessForSigil(sigil) != null);
  }

  //TODO: change String parameter into IWitness
  public IWitness getWitnessForSigil(String sigil) {
    IWitness internalGetWitnessForSigil = internalGetWitnessForSigil(sigil);
    if (internalGetWitnessForSigil == null) {
      throw new RuntimeException("Witness with " + sigil + " not found in this vertex!");
    }
    return internalGetWitnessForSigil;
  }

  private IWitness internalGetWitnessForSigil(String sigil) {
    Set<IWitness> set = tokenMap.keySet();
    for (IWitness w : set) {
      if (w.getSigil().equals(sigil)) {
        return w;
      }
    }
    return null;
  }

  public Set<IWitness> getWitnesses() {
    return tokenMap.keySet();
  }

}
