package eu.interedition.collatex.lab;

import eu.interedition.collatex.Token;

import java.util.List;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class SuffixTreeVertexModel {
  private final List<Token> tokens;
  private final int pathPosition;

  public SuffixTreeVertexModel(List<Token> tokens, int pathPosition) {
    this.tokens = tokens;
    this.pathPosition = pathPosition;
  }

  public List<Token> getTokens() {
    return tokens;
  }

  public int getPathPosition() {
    return pathPosition;
  }
}
