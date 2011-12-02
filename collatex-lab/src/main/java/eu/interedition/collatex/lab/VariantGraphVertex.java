package eu.interedition.collatex.lab;

import eu.interedition.collatex.interfaces.INormalizedToken;

import java.util.List;
import java.util.SortedSet;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class VariantGraphVertex {
  private SortedSet<INormalizedToken> tokens;

  public VariantGraphVertex(SortedSet<INormalizedToken> tokens) {
    this.tokens = tokens;
  }

  public SortedSet<INormalizedToken> getTokens() {
    return tokens;
  }
}
