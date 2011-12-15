package eu.interedition.collatex.lab;

import eu.interedition.collatex.interfaces.Token;

import java.util.SortedSet;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class EditGraphVertexModel {
  private final Token base;
  private final Token witness;

  public EditGraphVertexModel(Token witness, Token base) {
    this.base = base;
    this.witness = witness;
  }

  public Token getBase() {
    return base;
  }

  public Token getWitness() {
    return witness;
  }
}
