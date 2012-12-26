package eu.interedition.collatex.neo4j;

import static com.google.common.collect.Iterables.*;
import static java.util.Collections.singleton;
import static org.neo4j.graphdb.Direction.*;

import java.util.Iterator;
import java.util.Set;

import javax.annotation.Nullable;

import eu.interedition.collatex.VariantGraph;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.traversal.Evaluation;
import org.neo4j.graphdb.traversal.Evaluator;
import org.neo4j.kernel.Traversal;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;

import eu.interedition.collatex.Token;
import eu.interedition.collatex.Witness;
import eu.interedition.collatex.simple.SimpleToken;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class Neo4jVariantGraphVertex implements VariantGraph.Vertex {
  protected final Neo4jVariantGraph graph;
  protected final Node node;

  public Neo4jVariantGraphVertex(Neo4jVariantGraph graph, Node node) {
    this.graph = graph;
    this.node = node;
  }

  public Neo4jVariantGraphVertex(Neo4jVariantGraph graph, Set<Token> tokens) {
    this(graph, graph.database.createNode());
    setTokens(tokens);
  }

  @Override
  public Iterable<VariantGraph.Edge> incoming() {
    return incoming(null);
  }

  @Override
  public Iterable<VariantGraph.Edge> incoming(Set<Witness> witnesses) {
    return Iterables.filter(transform(node.getRelationships(Neo4jGraphRelationships.PATH, INCOMING), graph.edgeWrapper), Neo4jVariantGraphEdge.createTraversableFilter(witnesses));
  }

  @Override
  public Iterable<VariantGraph.Edge> outgoing() {
    return outgoing(null);
  }

  @Override
  public Iterable<VariantGraph.Edge> outgoing(Set<Witness> witnesses) {
    return Iterables.filter(transform(node.getRelationships(Neo4jGraphRelationships.PATH, OUTGOING), graph.edgeWrapper), Neo4jVariantGraphEdge.createTraversableFilter(witnesses));
  }

  @Override
  public Iterable<VariantGraph.Transposition> transpositions() {
    return transform(node.getRelationships(Neo4jGraphRelationships.TRANSPOSITION), new Function<Relationship, VariantGraph.Transposition>() {
      @Override
      public VariantGraph.Transposition apply(@Nullable Relationship relationship) {
        return graph.transpositionWrapper.apply(relationship.getStartNode());
      }
    });
  }

  @Override
  public Set<Token> tokens() {
    return tokens(null);
  }

  @Override
  public Set<Token> tokens(Set<Witness> witnesses) {
    return graph.adapter.getTokens(this, witnesses);
  }

  @Override
  public Set<Witness> witnesses() {
    final Set<Witness> witnesses = Sets.newHashSet();
    for (Token token : tokens()) {
      witnesses.add(token.getWitness());
    }
    return witnesses;
  }

  @Override
  public void add(Iterable<Token> tokens) {
    final Set<Token> tokenSet = Sets.newHashSet(tokens());
    Iterables.addAll(tokenSet, tokens);
    setTokens(tokenSet);
  }

  public void setTokens(Set<Token> tokens) {
    graph.adapter.setTokens(this, tokens);
  }

  @Override
  public String toString() {
    return Iterables.toString(tokens());
  }

  @Override
  public VariantGraph graph() {
    return graph;
  }

  public Node getNode() {
    return node;
  }

  @Override
  public void delete() {
    node.delete();
  }

  @Override
  public int hashCode() {
    return node.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj != null && obj instanceof Neo4jVariantGraphVertex) {
      return node.equals(((Neo4jVariantGraphVertex) obj).node);
    }
    return super.equals(obj);
  }
}
