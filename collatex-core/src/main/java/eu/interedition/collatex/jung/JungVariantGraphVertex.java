package eu.interedition.collatex.jung;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.Witness;
import org.apache.commons.collections15.bag.PredicatedBag;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Set;

import static eu.interedition.collatex.jung.JungVariantGraphEdge.IS_PATH;
import static eu.interedition.collatex.jung.JungVariantGraphEdge.IS_TRANSPOSITION;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class JungVariantGraphVertex implements VariantGraph.Vertex {
  private final JungVariantGraph graph;
  private final Set<Token> tokens = Sets.newHashSet();
  private int rank;

  public JungVariantGraphVertex(JungVariantGraph graph) {
    this.graph = graph;
  }

  @Override
  public Iterable<VariantGraph.Edge> incoming() {
    return incoming(null);
  }

  @Override
  public Iterable<VariantGraph.Edge> incoming(final Set<Witness> witnesses) {
    return paths(graph.getInEdges(this), witnesses);
  }

  @Override
  public Iterable<VariantGraph.Edge> outgoing() {
    return outgoing(null);
  }

  @Override
  public Iterable<VariantGraph.Edge> outgoing(Set<Witness> witnesses) {
    return paths(graph.getOutEdges(this), witnesses);
  }

  @Override
  public Iterable<VariantGraph.Transposition> transpositions() {
    return cast(Iterables.filter(Iterables.concat(incoming(), outgoing()), new Predicate<VariantGraph.Edge>() {

      @Override
      public boolean apply(@Nullable VariantGraph.Edge edge) {
        return IS_TRANSPOSITION.apply((JungVariantGraphEdge) edge);
      }
    }));
  }

  @Override
  public Set<Token> tokens() {
    return tokens(null);
  }

  @Override
  public Set<Token> tokens(final Set<Witness> witnesses) {
    return Collections.unmodifiableSet(Sets.filter(tokens, witnesses == null ? Predicates.<Token>alwaysTrue() : new Predicate<Token>() {
      @Override
      public boolean apply(@Nullable Token token) {
        return witnesses.contains(token.getWitness());
      }
    }));
  }

  @Override
  public Set<Witness> witnesses() {
    final Set<Witness> witnesses = Sets.newHashSet();
    for (VariantGraph.Edge edge : incoming()) {
      witnesses.addAll(edge.witnesses());
    }
    return witnesses;
  }

  @Override
  public void add(Iterable<Token> tokens) {
    Iterables.addAll(this.tokens, tokens);
  }

  @Override
  public int getRank() {
    return 0;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void setRank(int rank) {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public VariantGraph getGraph() {
    return graph;
  }

  @Override
  public void delete() {
    graph.removeVertex(this);
  }

  protected static Iterable<VariantGraph.Edge> paths(final Iterable<JungVariantGraphEdge> edges, final Set<Witness> witnesses) {
    return cast(Iterables.filter(edges, Predicates.and(IS_PATH, (witnesses == null
            ? Predicates.<JungVariantGraphEdge>alwaysTrue()
            : new Predicate<JungVariantGraphEdge>() {
      @Override
      public boolean apply(@Nullable JungVariantGraphEdge edge) {
        for (Witness edgeWitness : edge.witnesses()) {
          if (witnesses.contains(edgeWitness)) {
            return true;
          }
        }
        return false;
      }
    }))));
  }

  public static <T, C> Iterable<T> cast(Iterable<C> iterable) {
    Iterable iter = iterable;
    return iter;
  }
}
