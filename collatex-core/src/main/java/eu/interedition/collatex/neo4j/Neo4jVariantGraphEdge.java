package eu.interedition.collatex.neo4j;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.Witness;
import org.neo4j.graphdb.Relationship;

import java.util.Set;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class Neo4jVariantGraphEdge implements VariantGraph.Edge {
  protected final Neo4jVariantGraph graph;
  protected final Relationship relationship;

  public Neo4jVariantGraphEdge(Neo4jVariantGraph graph, Relationship relationship) {
    this.graph = graph;
    this.relationship = relationship;
  }

  public Neo4jVariantGraphEdge(Neo4jVariantGraph graph, Neo4jVariantGraphVertex from, Neo4jVariantGraphVertex to, Set<Witness> witnesses) {
    this(graph, from.getNode().createRelationshipTo(to.getNode(), Neo4jGraphRelationships.PATH));
    graph.adapter.setWitnesses(this, witnesses);
  }

  public boolean traversableWith(Set<Witness> witnesses) {
    if (witnesses == null || witnesses.isEmpty()) {
      return true;
    }
    final Set<Witness> edgeWitnesses = witnesses();
    for (Witness witness : witnesses) {
      if (edgeWitnesses.contains(witness)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public VariantGraph.Edge add(Set<Witness> witnesses) {
    graph.adapter.setWitnesses(this, Sets.union(witnesses(), witnesses));
    return this;
  }

  @Override
  public Set<Witness> witnesses() {
    return graph.adapter.getWitnesses(this);
  }

  public static Predicate<VariantGraph.Edge> createTraversableFilter(final Set<Witness> witnesses) {
    return new Predicate<VariantGraph.Edge>() {

      @Override
      public boolean apply(VariantGraph.Edge input) {
        return ((Neo4jVariantGraphEdge) input).traversableWith(witnesses);
      }
    };
  }

  @Override
  public VariantGraph graph() {
    return graph;
  }

  @Override
  public VariantGraph.Vertex from() {
    return graph.vertexWrapper.apply(relationship.getStartNode());
  }

  @Override
  public VariantGraph.Vertex to() {
    return graph.vertexWrapper.apply(relationship.getEndNode());
  }

  @Override
  public void delete() {
    relationship.delete();
  }

  @Override
  public int hashCode() {
    return relationship.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj != null && obj instanceof VariantGraph.Edge) {
      return relationship.equals(((Neo4jVariantGraphEdge) obj).relationship);
    }
    return super.equals(obj);
  }

  @Override
  public String toString() {
    return new StringBuilder(from().toString()).append(" -> ").append(to().toString()).toString();
  }
}
