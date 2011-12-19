package eu.interedition.collatex.lab;

import eu.interedition.collatex.Token;

import java.util.SortedSet;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class VariantGraphVertexModel {
  private final SortedSet<Token> tokens;
  private final int rank;

  public VariantGraphVertexModel(SortedSet<Token> tokens, int rank) {
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
