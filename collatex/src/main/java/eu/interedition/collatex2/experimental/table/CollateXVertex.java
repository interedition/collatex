package eu.interedition.collatex2.experimental.table;

import java.util.Map;

import com.google.common.collect.Maps;

import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IWitness;

public class CollateXVertex {
  private final String normalized;
  private final Map<IWitness, INormalizedToken> tokenMap;

  public CollateXVertex(String normalized) {
    this.normalized = normalized;
    this.tokenMap = Maps.newLinkedHashMap();
  }

  public INormalizedToken getToken(IWitness witness) {
    if (!tokenMap.containsKey(witness)) {
      throw new RuntimeException("WITNESS "+witness.getSigil()+" NOT FOUND IN THIS ARC!");
    }
    return tokenMap.get(witness);
  }
  
  public void addToken(IWitness witness, INormalizedToken token) {
    tokenMap.put(witness, token);
  }

  public String getNormalized() {
    return normalized;
  }
}
