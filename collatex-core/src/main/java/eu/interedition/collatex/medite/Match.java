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

  @Override
  public boolean equals(Object obj) {
    if (obj != null && obj instanceof Match) {
      return vertexRank == ((Match)obj).vertexRank;
    }
    return super.equals(obj);
  }

  @Override
  public int hashCode() {
    return vertexRank;
  }

  /**
  * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
  */
  static class WithEquivalence extends Match {
    final SuffixTree.EquivalenceClass equivalenceClass;

    WithEquivalence(VariantGraph.Vertex vertex, int vertexRank, SuffixTree.EquivalenceClass equivalenceClass) {
      super(vertex, vertexRank);
      this.equivalenceClass = equivalenceClass;
    }

    @Override
    public String toString() {
      return "{" + vertex + " -> " + equivalenceClass + "}";
    }
  }

  /**
   * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
   */
  static class WithTokenIndex extends Match {

    final int token;

    WithTokenIndex(VariantGraph.Vertex vertex, int vertexRank, int token) {
      super(vertex, vertexRank);
      this.token = token;
    }

    @Override
    public String toString() {
      return "{" + vertex + " -> " + token + "}";
    }
  }
}
