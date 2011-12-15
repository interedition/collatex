package eu.interedition.collatex.lab;

import eu.interedition.collatex.interfaces.Token;

import java.util.SortedSet;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class EditGraphVertexModel {
  private final Token base;
  private final Token witness;
  private final int weight;

  public EditGraphVertexModel(Token witness, Token base, int weight) {
    this.base = base;
    this.witness = witness;
    this.weight = weight;
  }

  public Token getBase() {
    return base;
  }

  public Token getWitness() {
    return witness;
  }

  public int getWeight() {
    return weight;
  }
}
