package eu.interedition.collatex.medite;

import eu.interedition.collatex.VariantGraph;

/**
* @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
*/
class TokenMatch extends Match {

  final int token;

  TokenMatch(VariantGraph.Vertex vertex, int vertexRank, int token) {
    super(vertex, vertexRank);
    this.token = token;
  }

  @Override
  public String toString() {
    return "{" + vertex + " -> " + token + "}";
  }
}
