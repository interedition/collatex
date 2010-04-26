package eu.interedition.collatex2.experimental.graph;

import eu.interedition.collatex2.interfaces.INormalizedToken;

public class VariantGraphNode implements IVariantGraphNode {
  private final INormalizedToken token;

  public VariantGraphNode(INormalizedToken token) {
    this.token = token;
  }

  @Override
  public String getNormalized() {
    return token.getNormalized();
  }

  @Override
  public INormalizedToken getToken() {
    return token;
  }

}
