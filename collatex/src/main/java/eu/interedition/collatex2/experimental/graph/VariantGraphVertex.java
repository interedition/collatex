package eu.interedition.collatex2.experimental.graph;

import eu.interedition.collatex2.experimental.table.CollateXVertex;
import eu.interedition.collatex2.interfaces.INormalizedToken;

public class VariantGraphVertex extends CollateXVertex implements IVariantGraphVertex {
  private final INormalizedToken token;

  public VariantGraphVertex(INormalizedToken token) {
    super(token.getNormalized());
    this.token = token;
  }

  @Override
  public String getNormalized() {
    return token.getNormalized();
  }


}
