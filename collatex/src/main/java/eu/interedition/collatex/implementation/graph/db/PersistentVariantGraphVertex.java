package eu.interedition.collatex.implementation.graph.db;

import org.neo4j.graphdb.Node;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class PersistentVariantGraphVertex {
  private final PersistentVariantGraph graph;
  private final Node node;

  public PersistentVariantGraphVertex(PersistentVariantGraph graph, Node node) {
    this.graph = graph;
    this.node = node;
  }

  @Override
  public int hashCode() {
    return node.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj != null && obj instanceof PersistentVariantGraphVertex) {
      return node.equals(((PersistentVariantGraphVertex)obj).node);
    }
    return super.equals(obj);
  }
}
