package eu.interedition.collatex.implementation.graph.db;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import eu.interedition.collatex.interfaces.IWitness;
import org.neo4j.graphdb.Relationship;

import java.util.SortedSet;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class VariantGraphEdge {
  private final VariantGraph graph;
  private final Relationship relationship;
  private static final String WITNESS_REFERENCE_KEY = "witnessReferences";

  public VariantGraphEdge(VariantGraph graph, Relationship relationship) {
    this.graph = graph;
    this.relationship = relationship;
  }

  public VariantGraphEdge(VariantGraph graph, VariantGraphVertex from, VariantGraphVertex to, SortedSet<IWitness> witnesses) {
    this(graph, from.getNode().createRelationshipTo(to.getNode(), VariantGraphRelationshipType.PATH));
    setWitnesses(witnesses);
  }

  public VariantGraphVertex from() {
    return graph.getVertexWrapper().apply(relationship.getStartNode());
  }

  public VariantGraphVertex to() {
    return graph.getVertexWrapper().apply(relationship.getEndNode());
  }

  public boolean traversableWith(SortedSet<IWitness> witnesses) {
    return (witnesses == null || witnesses.isEmpty() || Iterables.any(getWitnesses(), Predicates.in(witnesses)));
  }

  public VariantGraphEdge add(SortedSet<IWitness> witnesses) {
    final SortedSet<IWitness> registered = getWitnesses();
    registered.addAll(witnesses);
    setWitnesses(registered);
    return this;
  }

  public VariantGraph getGraph() {
    return graph;
  }

  public SortedSet<IWitness> getWitnesses() {
    return Sets.newTreeSet(graph.getWitnessResolver().resolve(getWitnessReferences()));
  }

  public void setWitnesses(SortedSet<IWitness> witnesses) {
    setWitnessReferences(graph.getWitnessResolver().resolve(witnesses));
  }

  public int[] getWitnessReferences() {
    return (int[]) relationship.getProperty(WITNESS_REFERENCE_KEY);
  }

  public void setWitnessReferences(int... references) {
    relationship.setProperty(WITNESS_REFERENCE_KEY, references);
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
    if (obj != null && obj instanceof VariantGraphEdge) {
      return relationship.equals(((VariantGraphEdge) obj).relationship);
    }
    return super.equals(obj);
  }

  public static Function<Relationship, VariantGraphEdge> createWrapper(final VariantGraph in) {
    return new Function<Relationship, VariantGraphEdge>() {
      @Override
      public VariantGraphEdge apply(Relationship input) {
        return new VariantGraphEdge(in, input);
      }
    };
  }

  public static Predicate<VariantGraphEdge> createTraversableFilter(final SortedSet<IWitness> witnesses) {
    return new Predicate<VariantGraphEdge>() {
      @Override
      public boolean apply(VariantGraphEdge input) {
        return input.traversableWith(witnesses);
      }
    };
  }

  public static final Function<VariantGraphEdge, String> TO_CONTENTS = new Function<VariantGraphEdge, String>() {
    @Override
    public String apply(VariantGraphEdge input) {
      return Joiner.on(", ").join(input.getWitnesses());
    }
  };
}
