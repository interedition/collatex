package eu.interedition.collatex.implementation.graph.db;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import org.neo4j.graphdb.Relationship;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class PersistentVariantGraphTransposition {

  private PersistentVariantGraph graph;
  private Relationship relationship;

  public PersistentVariantGraphTransposition(PersistentVariantGraph graph, Relationship relationship) {
    this.graph = graph;
    this.relationship = relationship;
  }

  public PersistentVariantGraphTransposition(PersistentVariantGraph graph, PersistentVariantGraphVertex from, PersistentVariantGraphVertex to) {
    this(graph, from.getNode().createRelationshipTo(to.getNode(), VariantGraphRelationshipType.TRANSPOSITION));
  }

  public PersistentVariantGraphVertex getStart() {
    return new PersistentVariantGraphVertex(graph, relationship.getStartNode());    
  }

  public PersistentVariantGraphVertex getEnd() {
    return new PersistentVariantGraphVertex(graph, relationship.getEndNode());
  }

  public PersistentVariantGraphVertex getOther(PersistentVariantGraphVertex vertex) {
    return new PersistentVariantGraphVertex(graph, relationship.getOtherNode(vertex.getNode()));
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
    if (obj != null && obj instanceof PersistentVariantGraphTransposition) {
      return relationship.equals(((PersistentVariantGraphTransposition)obj).relationship);
    }
    return super.equals(obj);
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this).addValue(getStart()).addValue(getEnd()).toString();
  }

  public static Function<Relationship, PersistentVariantGraphTransposition> createWrapper(final PersistentVariantGraph in) {
    return new Function<Relationship, PersistentVariantGraphTransposition>() {
      @Override
      public PersistentVariantGraphTransposition apply(Relationship input) {
        return new PersistentVariantGraphTransposition(in, input);
      }
    };
  }
}
