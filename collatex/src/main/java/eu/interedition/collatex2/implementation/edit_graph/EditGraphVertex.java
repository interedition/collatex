package eu.interedition.collatex2.implementation.edit_graph;

import eu.interedition.collatex2.interfaces.INormalizedToken;

public class EditGraphVertex {

  private final INormalizedToken normalizedToken;

  public EditGraphVertex(INormalizedToken normalizedToken) {
    this.normalizedToken = normalizedToken;
  }

  public INormalizedToken getToken() {
    return normalizedToken;
  }

  @Override
  public String toString() {
    return normalizedToken.toString();
  }
}
