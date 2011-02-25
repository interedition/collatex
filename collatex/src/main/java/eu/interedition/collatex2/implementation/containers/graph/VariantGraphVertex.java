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

  @Override
  public boolean containsWitness(IWitness witness) {
    return tokenMap.containsKey(witness);
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
  public String getContent() {
    throw new RuntimeException("Do not call this method! Call getToken(IWitness).getContent() instead.");
  }

  public INormalizedToken getVertexKey() {
    return vertexKey;
  }

}
