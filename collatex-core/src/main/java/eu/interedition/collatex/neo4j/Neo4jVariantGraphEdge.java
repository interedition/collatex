package eu.interedition.collatex.neo4j;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import eu.interedition.collatex.Witness;
import org.neo4j.graphdb.Relationship;

import java.util.Set;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class Neo4jVariantGraphEdge {
  private static final String WITNESS_REFERENCE_KEY = "witnessReferences";
  protected final Neo4jVariantGraph graph;
  protected final Relationship relationship;

  public Neo4jVariantGraphEdge(Neo4jVariantGraph graph, Relationship relationship) {
    this.graph = graph;
    this.relationship = relationship;
  }

  public Neo4jVariantGraphEdge(Neo4jVariantGraph graph, Neo4jVariantGraphVertex from, Neo4jVariantGraphVertex to, Set<Witness> witnesses) {
    this(graph, from.getNode().createRelationshipTo(to.getNode(), GraphRelationshipType.PATH));
    setWitnessReferences(this.graph.getWitnessMapper().map(witnesses));
  }

  public boolean traversableWith(Set<Witness> witnesses) {
    return (witnesses == null || witnesses.isEmpty() || traversableWith(graph.getWitnessMapper().map(witnesses)));
  }

  public boolean traversableWith(int[] witnesses) {
    if (witnesses == null || witnesses.length == 0) {
      return true;
    }

    final int[] witnessReferences = getWitnessReferences();
    for (int wrc = 0; wrc < witnessReferences.length; wrc++) {
      for (int wc = 0; wc < witnesses.length; wc++) {
        if (witnessReferences[wrc] == witnesses[wc]) {
          return true;
        }
      }
    }
    return false;
  }

  public Neo4jVariantGraphEdge add(Set<Witness> witnesses) {
    setWitnessReferences(graph.getWitnessMapper().map(Sets.union(witnesses(), witnesses)));
    return this;
  }

  public Set<Witness> witnesses() {
    return graph.getWitnessMapper().map(getWitnessReferences());
  }

  public int[] getWitnessReferences() {
    return (int[]) relationship.getProperty(WITNESS_REFERENCE_KEY);
  }

  public void setWitnessReferences(int... references) {
    relationship.setProperty(WITNESS_REFERENCE_KEY, references);
  }

  public static Function<Relationship, Neo4jVariantGraphEdge> createWrapper(final Neo4jVariantGraph in) {
    return new Function<Relationship, Neo4jVariantGraphEdge>() {
      @Override
      public Neo4jVariantGraphEdge apply(Relationship input) {
        return new Neo4jVariantGraphEdge(in, input);
      }
    };
  }

  public static Predicate<Neo4jVariantGraphEdge> createTraversableFilter(final Set<Witness> witnesses) {
    return new Predicate<Neo4jVariantGraphEdge>() {
      private int[] witnessReferences;

      @Override
      public boolean apply(Neo4jVariantGraphEdge input) {
        if (witnessReferences == null) {
          witnessReferences = ((witnesses == null || witnesses.isEmpty()) ? new int[0] : input.getGraph().getWitnessMapper().map(witnesses));
        }
        return input.traversableWith(witnessReferences);
      }
    };
  }

  public static final Function<Neo4jVariantGraphEdge, String> TO_CONTENTS = new Function<Neo4jVariantGraphEdge, String>() {
    @Override
    public String apply(Neo4jVariantGraphEdge input) {
      return Joiner.on(", ").join(Ordering.from(Witness.SIGIL_COMPARATOR).sortedCopy(input.witnesses()));
    }
  };

  public Neo4jVariantGraph getGraph() {
    return graph;
  }

  public Neo4jVariantGraphVertex from() {
    return graph.getVertexWrapper().apply(relationship.getStartNode());
  }

  public Neo4jVariantGraphVertex to() {
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
    if (obj != null && obj instanceof Neo4jVariantGraphEdge) {
      return relationship.equals(((Neo4jVariantGraphEdge) obj).relationship);
    }
    return super.equals(obj);
  }

  @Override
  public String toString() {
    return new StringBuilder(from().toString()).append(" -> ").append(to().toString()).toString();
  }
}
