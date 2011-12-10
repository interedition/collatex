package eu.interedition.collatex.implementation.graph.db;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import org.neo4j.graphdb.Relationship;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class VariantGraphTransposition {

  private VariantGraph graph;
  private Relationship relationship;

  public VariantGraphTransposition(VariantGraph graph, Relationship relationship) {
    this.graph = graph;
    this.relationship = relationship;
  }

  public VariantGraphTransposition(VariantGraph graph, VariantGraphVertex from, VariantGraphVertex to) {
    this(graph, from.getNode().createRelationshipTo(to.getNode(), VariantGraphRelationshipType.TRANSPOSITION));
  }

  public VariantGraphVertex from() {
    return new VariantGraphVertex(graph, relationship.getStartNode());
  }

  public VariantGraphVertex to() {
    return new VariantGraphVertex(graph, relationship.getEndNode());
  }

  public VariantGraphVertex other(VariantGraphVertex vertex) {
    return new VariantGraphVertex(graph, relationship.getOtherNode(vertex.getNode()));
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
    if (obj != null && obj instanceof VariantGraphTransposition) {
      return relationship.equals(((VariantGraphTransposition)obj).relationship);
    }
    return super.equals(obj);
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this).addValue(from()).addValue(to()).toString();
  }

  public static Function<Relationship, VariantGraphTransposition> createWrapper(final VariantGraph in) {
    return new Function<Relationship, VariantGraphTransposition>() {
      @Override
      public VariantGraphTransposition apply(Relationship input) {
        return new VariantGraphTransposition(in, input);
      }
    };
  }
}
