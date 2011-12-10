package eu.interedition.collatex.lab;

import eu.interedition.collatex.interfaces.Token;

import java.util.SortedSet;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class VariantGraphVertex {
  private final SortedSet<Token> tokens;
  private final int rank;

  public VariantGraphVertex(SortedSet<Token> tokens, int rank) {
    this.tokens = tokens;
    this.rank = rank;
  }

  public SortedSet<Token> getTokens() {
    return tokens;
  }

  public int getRank() {
    return rank;
  }
}
