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
public class PersistentVariantGraphEdge {
  private final PersistentVariantGraph graph;
  private final Relationship relationship;
  private static final String WITNESS_REFERENCE_KEY = "witnessReferences";

  public PersistentVariantGraphEdge(PersistentVariantGraph graph, Relationship relationship) {
    this.graph = graph;
    this.relationship = relationship;
  }

  public PersistentVariantGraphEdge(PersistentVariantGraph graph, PersistentVariantGraphVertex from, PersistentVariantGraphVertex to, SortedSet<IWitness> witnesses) {
    this(graph, from.getNode().createRelationshipTo(to.getNode(), VariantGraphRelationshipType.PATH));
    setWitnesses(witnesses);
  }

  public PersistentVariantGraph getGraph() {
    return graph;
  }

  public PersistentVariantGraphVertex getStart() {
    return graph.getVertexWrapper().apply(relationship.getStartNode());
  }

  public PersistentVariantGraphVertex getEnd() {
    return graph.getVertexWrapper().apply(relationship.getEndNode());
  }

  public boolean canBeTraversed(SortedSet<IWitness> witnesses) {
    return witnesses == null || witnesses.isEmpty() || Iterables.any(getWitnesses(), Predicates.in(witnesses));
  }

  public PersistentVariantGraphEdge add(SortedSet<IWitness> witnesses) {
    final SortedSet<IWitness> registered = getWitnesses();
    registered.addAll(witnesses);
    setWitnesses(registered);
    return this;
  }

  public int[] getWitnessReferences() {
    return (int[]) relationship.getProperty(WITNESS_REFERENCE_KEY);
  }

  public void setWitnessReferences(int... references) {
    relationship.setProperty(WITNESS_REFERENCE_KEY, references);
  }

  public SortedSet<IWitness> getWitnesses() {
    return Sets.newTreeSet(graph.getWitnessResolver().resolve(getWitnessReferences()));
  }

  public void setWitnesses(SortedSet<IWitness> witnesses) {
    setWitnessReferences(graph.getWitnessResolver().resolve(witnesses));
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
    if (obj != null && obj instanceof PersistentVariantGraphEdge) {
      return relationship.equals(((PersistentVariantGraphEdge) obj).relationship);
    }
    return super.equals(obj);
  }

  public static Function<Relationship, PersistentVariantGraphEdge> createWrapper(final PersistentVariantGraph in) {
    return new Function<Relationship, PersistentVariantGraphEdge>() {
      @Override
      public PersistentVariantGraphEdge apply(Relationship input) {
        return new PersistentVariantGraphEdge(in, input);
      }
    };
  }

  public static Predicate<PersistentVariantGraphEdge> createTraversableFilter(final SortedSet<IWitness> witnesses) {
    return new Predicate<PersistentVariantGraphEdge>() {
      @Override
      public boolean apply(PersistentVariantGraphEdge input) {
        return input.canBeTraversed(witnesses);
      }
    };
  }

  public static final Function<PersistentVariantGraphEdge, String> TO_CONTENTS = new Function<PersistentVariantGraphEdge, String>() {
    @Override
    public String apply(PersistentVariantGraphEdge input) {
      return Joiner.on(", ").join(input.getWitnesses());
    }
  };
}
