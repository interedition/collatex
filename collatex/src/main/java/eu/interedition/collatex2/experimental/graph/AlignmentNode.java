package eu.interedition.collatex2.experimental.graph;

import eu.interedition.collatex2.interfaces.INormalizedToken;

public class AlignmentNode implements IAlignmentNode {
  private final INormalizedToken token;

  public AlignmentNode(INormalizedToken token) {
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
