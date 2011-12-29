package eu.interedition.collatex.lab;

import eu.interedition.collatex.Token;

import java.util.Set;
import java.util.SortedSet;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class EditGraphVertexModel {
  private final Set<Token> base;
  private final Token witness;

  public EditGraphVertexModel(Token witness, Set<Token> base) {
    this.base = base;
    this.witness = witness;
  }

  public Set<Token> getBase() {
    return base;
  }

  public Token getWitness() {
    return witness;
  }
}
