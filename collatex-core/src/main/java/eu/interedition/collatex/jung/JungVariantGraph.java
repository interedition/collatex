package eu.interedition.collatex.jung;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.collect.RowSortedTable;
import com.google.common.collect.Sets;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.Witness;
import eu.interedition.collatex.util.VariantGraphs;

import java.util.Collections;
import java.util.Set;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class JungVariantGraph extends DirectedSparseGraph<JungVariantGraphVertex, JungVariantGraphEdge> implements VariantGraph {

  final JungVariantGraphVertex start;
  final JungVariantGraphVertex end;
  final Multimap<JungVariantGraphVertex, JungVariantGraphTransposition> transpositionIndex = HashMultimap.create();

  public JungVariantGraph() {
    super();
    addVertex(this.start = new JungVariantGraphVertex(this, Collections.<Token>emptySet()));
    addVertex(this.end = new JungVariantGraphVertex(this, Collections.<Token>emptySet()));
    connect(this.start, this.end, Collections.<Witness>emptySet());
  }

  @Override
  public Vertex getStart() {
    return start;
  }

  @Override
  public Vertex getEnd() {
    return end;
  }

  @Override
  public Set<Transposition> transpositions() {
    return Sets.newHashSet(Iterables.filter(transpositionIndex.values(), Transposition.class));
  }

  @Override
  public Iterable<Vertex> vertices() {
    return vertices(null);
  }

  @Override
  public Iterable<Vertex> vertices(Set<Witness> witnesses) {
    return VariantGraphs.vertices(this, witnesses);
  }

  @Override
  public Iterable<Edge> edges() {
    return edges(null);
  }

  @Override
  public Iterable<Edge> edges(Set<Witness> witnesses) {
    return VariantGraphs.edges(this, witnesses);
  }

  @Override
  public Vertex add(Token token) {
    final JungVariantGraphVertex vertex = new JungVariantGraphVertex(this, Collections.singleton(token));
    addVertex(vertex);
    return vertex;
  }

  @Override
  public Edge connect(Vertex from, Vertex to, Set<Witness> witnesses) {
    Preconditions.checkArgument(!from.equals(to));

    if (from.equals(start)) {
      final Edge startEndEdge = edgeBetween(start, end);
      if (startEndEdge != null) {
        startEndEdge.delete();
      }
    }

    for (Edge e : from.outgoing()) {
      if (to.equals(e.to())) {
        return e.add(witnesses);
      }
    }

    final JungVariantGraphEdge edge = new JungVariantGraphEdge(this, witnesses);
    addEdge(edge, (JungVariantGraphVertex) from, (JungVariantGraphVertex) to);
    return edge;
  }

  @Override
  public Transposition transpose(Set<Vertex> vertices) {
    Preconditions.checkArgument(!vertices.isEmpty());
    for (Transposition transposition : vertices.iterator().next().transpositions()) {
      if (Sets.newHashSet(transposition).equals(vertices)) {
        return transposition;
      }
    }
    return new JungVariantGraphTransposition(this, vertices);
  }

  @Override
  public Edge edgeBetween(Vertex a, Vertex b) {
    return findEdge((JungVariantGraphVertex) a, (JungVariantGraphVertex) b);
  }

  @Override
  public Set<Witness> witnesses() {
    Set<Witness> witnesses = Sets.newHashSet();
    for (Edge edge : start.outgoing()) {
      witnesses.addAll(edge.witnesses());
    }
    return witnesses;
  }

  @Override
  public String toString() {
    return Iterables.toString(witnesses());
  }
}
