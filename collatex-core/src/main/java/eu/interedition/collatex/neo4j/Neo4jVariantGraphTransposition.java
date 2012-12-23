package eu.interedition.collatex.neo4j;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import eu.interedition.collatex.VariantGraph;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import com.google.common.base.Objects;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.Set;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class Neo4jVariantGraphTransposition implements VariantGraph.Transposition {

  private final Neo4jVariantGraph graph;
  private final Node node;

  public Neo4jVariantGraphTransposition(Neo4jVariantGraph graph, Node node) {
    this.graph = graph;
    this.node = node;
  }

  public Neo4jVariantGraphTransposition(Neo4jVariantGraph graph, Set<VariantGraph.Vertex> vertices) {
    this(graph, graph.database.createNode());
    for (Neo4jVariantGraphVertex vertex : Iterables.filter(vertices, Neo4jVariantGraphVertex.class)) {
      this.node.createRelationshipTo(vertex.node, Neo4jGraphRelationships.TRANSPOSITION);
    }
  }

  @Override
  public Iterator<VariantGraph.Vertex> iterator() {
    return Iterators.transform(node.getRelationships(Neo4jGraphRelationships.TRANSPOSITION).iterator(), new Function<Relationship, VariantGraph.Vertex>() {
      @Override
      public VariantGraph.Vertex apply(@Nullable Relationship relationship) {
        return graph.vertexWrapper.apply(relationship.getEndNode());
      }
    });
  }

  @Override
  public void delete() {
    for (Relationship r : node.getRelationships(Neo4jGraphRelationships.TRANSPOSITION)) {
      r.delete();
    }
    node.delete();
  }

  @Override
  public int hashCode() {
    return node.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj != null && obj instanceof Neo4jVariantGraphTransposition) {
      return node.equals(((Neo4jVariantGraphTransposition) obj).node);
    }
    return super.equals(obj);
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this).addValue(node).toString();
  }
}
