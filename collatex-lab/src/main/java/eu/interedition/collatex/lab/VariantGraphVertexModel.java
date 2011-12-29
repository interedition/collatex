package eu.interedition.collatex.lab;

import eu.interedition.collatex.Token;

import java.util.Set;
import java.util.SortedSet;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class VariantGraphVertexModel {
  private final Set<Token> tokens;
  private final int rank;

  public VariantGraphVertexModel(Set<Token> tokens, int rank) {
    this.tokens = tokens;
    this.rank = rank;
  }

  public Set<Token> getTokens() {
    return tokens;
  }

  public int getRank() {
    return rank;
  }
}
