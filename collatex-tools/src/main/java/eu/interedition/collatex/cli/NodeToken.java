package eu.interedition.collatex.cli;

import eu.interedition.collatex.simple.SimpleToken;
import eu.interedition.collatex.simple.SimpleWitness;
import org.w3c.dom.Node;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class NodeToken extends SimpleToken {
  final Node node;

  public NodeToken(SimpleWitness witness, String content, String normalized, Node node) {
    super(witness, content, normalized);
    this.node = node;
  }
}
