package eu.interedition.collatex.medite;

import eu.interedition.collatex.VariantGraph;

/**
* @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
*/
class EquivalenceClassMatch extends Match {
  final SuffixTree.EquivalenceClass equivalenceClass;

  EquivalenceClassMatch(VariantGraph.Vertex vertex, int vertexRank, SuffixTree.EquivalenceClass equivalenceClass) {
    super(vertex, vertexRank);
    this.equivalenceClass = equivalenceClass;
  }

  @Override
  public String toString() {
    return "{" + vertex + " -> " + equivalenceClass + "}";
  }
}
