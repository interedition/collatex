package eu.interedition.collatex.schmidt;

import eu.interedition.collatex.VariantGraph;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
class Match {
  final VariantGraph.Vertex vertex;
  final Integer token;

  Match(VariantGraph.Vertex vertex, Integer token) {
    this.vertex = vertex;
    this.token = token;
  }

  @Override
  public String toString() {
    return "{" + vertex + " -> " + token + "}";
  }
}
