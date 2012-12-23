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
public class JungVariantGraphEdge implements VariantGraph.Edge {

  final JungVariantGraph graph;
  final Set<Witness> witnesses;

  public JungVariantGraphEdge(JungVariantGraph graph, Set<Witness> witnesses) {
    this.graph = graph;
    this.witnesses = Sets.newHashSet(witnesses);
  }

  @Override
  public VariantGraph.Edge add(Set<Witness> witnesses) {
    witnesses.addAll(witnesses);
    return this;
  }

  @Override
  public Set<Witness> witnesses() {
    return Collections.unmodifiableSet(witnesses);
  }

  @Override
  public VariantGraph graph() {
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
  public void delete() {
    graph.removeEdge(this);
  }
}
