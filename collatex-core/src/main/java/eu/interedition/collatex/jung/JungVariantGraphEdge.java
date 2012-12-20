package eu.interedition.collatex.jung;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Sets;
import edu.uci.ics.jung.graph.util.Pair;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.Witness;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Set;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class JungVariantGraphEdge implements VariantGraph.Edge, VariantGraph.Transposition {

  final JungVariantGraph graph;
  final boolean transposition;
  final Set<Witness> witnesses;

  public JungVariantGraphEdge(JungVariantGraph graph, Set<Witness> witnesses, boolean transposition) {
    this.graph = graph;
    this.transposition = transposition;
    this.witnesses = Sets.newHashSet(witnesses);
  }

  @Override
  public boolean traversableWith(Set<Witness> witnesses) {
    Preconditions.checkState(!transposition, "Transpositions are traversable independent of a witness set");

    for (Witness witness : witnesses) {
      if (witnesses.contains(witness)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public VariantGraph.Edge add(Set<Witness> witnesses) {
    Preconditions.checkState(!transposition, "Witnesses cannot be registered to transpositions");
    witnesses.addAll(witnesses);
    return this;
  }

  @Override
  public Set<Witness> witnesses() {
    return Collections.unmodifiableSet(witnesses);
  }

  @Override
  public VariantGraph getGraph() {
    return graph;
  }

  @Override
  public VariantGraph.Vertex from() {
    return graph.getEndpoints(this).getFirst();
  }

  @Override
  public VariantGraph.Vertex to() {
    return graph.getEndpoints(this).getSecond();
  }

  @Override
  public int getId() {
    throw new UnsupportedOperationException();
  }

  @Override
  public VariantGraph.Vertex other(VariantGraph.Vertex vertex) {
    final Pair<JungVariantGraphVertex> endpoints = graph.getEndpoints(this);
    final JungVariantGraphVertex first = endpoints.getFirst();
    final JungVariantGraphVertex second = endpoints.getSecond();

    if (first.equals(vertex)) {
      return second;
    } else if (second.equals(vertex)) {
      return first;
    } else {
      throw new IllegalArgumentException(vertex.toString());
    }
  }

  @Override
  public void delete() {
    graph.removeEdge(this);
  }

  static final Predicate<JungVariantGraphEdge> IS_TRANSPOSITION = new Predicate<JungVariantGraphEdge>() {
    @Override
    public boolean apply(@Nullable JungVariantGraphEdge edge) {
      return edge.transposition;
    }
  };

  static final Predicate<JungVariantGraphEdge> IS_PATH = Predicates.not(IS_TRANSPOSITION);
}
