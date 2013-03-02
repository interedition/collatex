package eu.interedition.collatex.cli;

import eu.interedition.collatex.Witness;
import org.w3c.dom.Document;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class DocumentWitness implements Witness {

  final String sigil;
  final Document document;

  public DocumentWitness(String sigil, Document document) {
    this.sigil = sigil;
    this.document = document;
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
