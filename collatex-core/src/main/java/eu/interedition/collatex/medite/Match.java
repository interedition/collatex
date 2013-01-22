package eu.interedition.collatex.medite;

import eu.interedition.collatex.VariantGraph;

/**
* @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
*/
abstract class Match implements Comparable<Match> {
  final VariantGraph.Vertex vertex;
  final int vertexRank;

  Match(VariantGraph.Vertex vertex, int vertexRank) {
    this.vertex = vertex;
    this.vertexRank = vertexRank;
  }

  @Override
  public int compareTo(Match o) {
    return (vertexRank - o.vertexRank);
  }

}
