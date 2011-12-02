package eu.interedition.collatex.lab;

import eu.interedition.collatex.interfaces.INormalizedToken;

import java.util.List;
import java.util.SortedSet;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class VariantGraphVertex {
  private final SortedSet<INormalizedToken> tokens;
  private final int rank;

  public VariantGraphVertex(SortedSet<INormalizedToken> tokens, int rank) {
    this.tokens = tokens;
    this.rank = rank;
  }

  public SortedSet<INormalizedToken> getTokens() {
    return tokens;
  }

  public int getRank() {
    return rank;
  }
}
