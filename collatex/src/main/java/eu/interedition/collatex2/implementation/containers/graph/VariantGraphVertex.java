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
  private final INormalizedToken vertexKey;

  public VariantGraphVertex(String normalized, INormalizedToken vertexKey) {
    this.normalized = normalized;
    this.vertexKey = vertexKey;
    this.tokenMap = Maps.newLinkedHashMap();
  }

  @Override
  public String getNormalized() {
    return normalized;
  }

  @Override
  public INormalizedToken getToken(IWitness witness) {
    if (!tokenMap.containsKey(witness)) {
      throw new RuntimeException("TOKEN FOR WITNESS " + witness.getSigil() + " NOT FOUND IN VERTEX " + getNormalized() + "!");
    }
    return tokenMap.get(witness);
  }

  @Override
  public void addToken(IWitness witness, INormalizedToken token) {
    tokenMap.put(witness, token);
  }

  //TODO: change String parameter into IWitness
  @Override
  public boolean containsWitness(String sigil) {
    return (internalGetWitnessForSigil(sigil) != null);
  }

  //TODO: change String parameter into IWitness
  @Override
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

  @Override
  public Set<IWitness> getWitnesses() {
    return tokenMap.keySet();
  }

  @Override
  public String toString() {
    return "[" + getNormalized() + "]";
  }

  @Override
  public String getSigil() {
    throw new RuntimeException("WRONG!");
  }

  @Override
  public String getContent() {
    throw new RuntimeException("Do not call this method! Call getToken(IWitness).getContent() instead.");
  }

  public INormalizedToken getVertexKey() {
    return vertexKey;
  }
}
