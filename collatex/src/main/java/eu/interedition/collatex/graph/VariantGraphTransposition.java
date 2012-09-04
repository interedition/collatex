package eu.interedition.collatex.graph;

import org.neo4j.graphdb.Relationship;

import com.google.common.base.Function;
import com.google.common.base.Objects;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class VariantGraphTransposition {

  private static final String TRANSPOSITIONID_KEY = "transpositionId";
  private final VariantGraph graph;
  private final Relationship relationship;

  public VariantGraphTransposition(VariantGraph graph, Relationship relationship, int transpositionId) {
    this.graph = graph;
    this.relationship = relationship;
    this.relationship.setProperty(TRANSPOSITIONID_KEY, transpositionId);
  }

  public VariantGraphTransposition(VariantGraph graph, Relationship relationship) {
    this.graph = graph;
    this.relationship = relationship;
  }

  public VariantGraphTransposition(VariantGraph graph, VariantGraphVertex from, VariantGraphVertex to, int transpositionId) {
    this(graph, from.getNode().createRelationshipTo(to.getNode(), GraphRelationshipType.TRANSPOSITION), transpositionId);
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
      return relationship.equals(((VariantGraphTransposition) obj).relationship);
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

  public int getId() {
    return (Integer) relationship.getProperty(TRANSPOSITIONID_KEY, 0);
  }

}
