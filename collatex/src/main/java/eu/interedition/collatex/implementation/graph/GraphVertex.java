package eu.interedition.collatex.implementation.graph;

import org.neo4j.graphdb.Node;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public abstract class GraphVertex<G extends Graph> {
  protected final G graph;
  protected final Node node;

  public GraphVertex(G graph, Node node) {
    this.graph = graph;
    this.node = node;
  }

  public G getGraph() {
    return graph;
  }

  public Node getNode() {
    return node;
  }

  public void delete() {
    node.delete();
  }

  @Override
  public int hashCode() {
    return node.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj != null && obj instanceof VariantGraphVertex) {
      return node.equals(((GraphVertex)obj).node);
    }
    return super.equals(obj);
  }
}
