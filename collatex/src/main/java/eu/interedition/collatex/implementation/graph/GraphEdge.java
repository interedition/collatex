package eu.interedition.collatex.implementation.graph;

import org.neo4j.graphdb.Relationship;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public abstract class GraphEdge<G extends Graph<V, ?>, V extends GraphVertex> {
  protected final G graph;
  protected final Relationship relationship;

  public GraphEdge(G graph, Relationship relationship) {
    this.graph = graph;
    this.relationship = relationship;
  }

  public G getGraph() {
    return graph;
  }

  public V from() {
    return graph.getVertexWrapper().apply(relationship.getStartNode());
  }

  public V to() {
    return graph.getVertexWrapper().apply(relationship.getEndNode());
  }

  public void delete() {
    relationship.delete();
  }

  @Override
  public int hashCode() {
    return relationship.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj != null && obj instanceof GraphEdge) {
      return relationship.equals(((GraphEdge) obj).relationship);
    }
    return super.equals(obj);
  }


}
