package eu.interedition.collatex.cli;

import eu.interedition.collatex.Witness;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class TextWitness implements Witness {

  public final String sigil;
  public final String content;

  public TextWitness(String sigil, String content) {
    this.sigil = sigil;
    this.content = content;
  }

  @Override
  public String getSigil() {
    return sigil;
  }

  @Override
  public String toString() {
    return sigil;
  }
}
