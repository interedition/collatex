package eu.interedition.collatex.lab;

import eu.interedition.collatex.interfaces.INormalizedToken;

import java.util.List;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class VariantGraphVertex {
  private List<INormalizedToken> tokens;

  public VariantGraphVertex(List<INormalizedToken> tokens) {
    this.tokens = tokens;
  }

  public List<INormalizedToken> getTokens() {
    return tokens;
  }
}
