package eu.interedition.collatex.jung;

import com.google.common.collect.Iterables;
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

  public JungVariantGraph() {
    super();
    addVertex(this.start = new JungVariantGraphVertex(this));
    addVertex(this.end = new JungVariantGraphVertex(this));
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
    final Set<Transposition> transpositions = Sets.newHashSet();
    for (Vertex v : vertices()) {
      Iterables.addAll(transpositions, v.transpositions());
    }
    return transpositions;
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
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public Edge connect(Vertex from, Vertex to, Set<Witness> witnesses) {
    final JungVariantGraphEdge edge = new JungVariantGraphEdge(this, witnesses, false);
    addEdge(edge, (JungVariantGraphVertex) from, (JungVariantGraphVertex) to);
    return edge;
  }

  @Override
  public Transposition transpose(Vertex from, Vertex to, int transpId) {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public boolean isNear(Vertex a, Vertex b) {
    return false;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public boolean verticesAreAdjacent(Vertex a, Vertex b) {
    return false;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public Edge edgeBetween(Vertex a, Vertex b) {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public Set<Witness> witnesses() {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public VariantGraph join() {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public VariantGraph rank() {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public VariantGraph adjustRanksForTranspositions() {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public Iterable<Set<Vertex>> ranks() {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public Iterable<Set<Vertex>> ranks(Set<Witness> witnesses) {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public RowSortedTable<Integer, Witness, Set<Token>> toTable() {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }
}
