package eu.interedition.collatex.jung;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.Witness;

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
    this.witnesses.addAll(witnesses);
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

  @Override
  public String toString() {
    return Iterables.toString(witnesses);
  }
}
