package eu.interedition.collatex.implementation.graph.db;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import eu.interedition.collatex.interfaces.Token;
import eu.interedition.collatex.interfaces.IWitness;
import org.neo4j.graphdb.Node;

import java.util.Collections;
import java.util.Iterator;
import java.util.SortedSet;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.transform;
import static eu.interedition.collatex.implementation.graph.db.VariantGraphEdge.createTraversableFilter;
import static eu.interedition.collatex.implementation.graph.db.VariantGraphRelationshipType.PATH;
import static eu.interedition.collatex.implementation.graph.db.VariantGraphRelationshipType.TRANSPOSITION;
import static org.neo4j.graphdb.Direction.INCOMING;
import static org.neo4j.graphdb.Direction.OUTGOING;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class VariantGraphVertex {
  private final VariantGraph graph;
  private final Node node;
  private static final String TOKEN_REFERENCE_KEY = "tokenReferences";
  private static final String RANK_KEY = "rank";

  public VariantGraphVertex(VariantGraph graph, Node node) {
    this.graph = graph;
    this.node = node;
  }

  public VariantGraphVertex(VariantGraph graph, SortedSet<Token> tokens) {
    this(graph, graph.getDb().createNode());
    setTokens(tokens);
  }

  public VariantGraph getGraph() {
    return graph;
  }

  public Node getNode() {
    return node;
  }

  public Iterable<VariantGraphEdge> incoming() {
    return incoming(null);
  }

  public Iterable<VariantGraphEdge> incoming(SortedSet<IWitness> witnesses) {
    return filter(transform(node.getRelationships(PATH, INCOMING), graph.getEdgeWrapper()), createTraversableFilter(witnesses));
  }

  public Iterable<VariantGraphEdge> outgoing() {
    return outgoing(null);
  }

  public Iterable<VariantGraphEdge> outgoing(SortedSet<IWitness> witnesses) {
    return filter(transform(node.getRelationships(PATH, OUTGOING), graph.getEdgeWrapper()), createTraversableFilter(witnesses));
  }

  public Iterable<VariantGraphTransposition> transpositions() {
    return transform(node.getRelationships(TRANSPOSITION), graph.getTranspositionWrapper());
  }

  public SortedSet<Token> tokens() {
    return tokens(null);
  }

  public SortedSet<Token> tokens(SortedSet<IWitness> witnesses) {
    final SortedSet<Token> tokens = Sets.newTreeSet(graph.getTokenResolver().resolve(getTokenReferences()));
    if (witnesses != null && !witnesses.isEmpty()) {
      for (Iterator<Token> tokenIt = tokens.iterator(); tokenIt.hasNext(); ) {
        final Token token = tokenIt.next();
        if (!witnesses.contains(token.getWitness())) {
          tokenIt.remove();
        }
      }
    }
    return tokens;
  }

  public SortedSet<IWitness> witnesses() {
    final SortedSet<IWitness> witnesses = Sets.newTreeSet();
    for (Token token : tokens()) {
      witnesses.add(token.getWitness());
    }
    return witnesses;
  }

  public void add(Iterable<Token> tokens) {
    final SortedSet<Token> tokenSet = Sets.newTreeSet(tokens());
    Iterables.addAll(tokenSet, tokens);
    setTokens(tokenSet);
  }

  public void setTokens(SortedSet<Token> tokens) {
    setTokenReferences(graph.getTokenResolver().resolve(tokens));
  }

  public int getRank() {
    return (Integer) node.getProperty(RANK_KEY);
  }

  public void setRank(int rank) {
    node.setProperty(RANK_KEY, rank);
  }

  public int[] getTokenReferences() {
    return (int[]) node.getProperty(TOKEN_REFERENCE_KEY);
  }

  public void setTokenReferences(int... references) {
    node.setProperty(TOKEN_REFERENCE_KEY, references);
  }

  public void delete() {
    node.delete();
  }

  @Override
  public int hashCode() {
    return node.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj != null && obj instanceof VariantGraphVertex) {
      return node.equals(((VariantGraphVertex)obj).node);
    }
    return super.equals(obj);
  }

  @Override
  public String toString() {
    return Iterables.toString(tokens());
  }

  public static Function<Node, VariantGraphVertex> createWrapper(final VariantGraph in) {
    return new Function<Node, VariantGraphVertex>() {
      @Override
      public VariantGraphVertex apply(Node input) {
        return new VariantGraphVertex(in, input);
      }
    };
  }

  public static final Function<VariantGraphVertex, String> TO_CONTENTS = new Function<VariantGraphVertex, String>() {
    @Override
    public String apply(VariantGraphVertex input) {
      final SortedSet<IWitness> witnesses = input.witnesses();
      if (witnesses.isEmpty()) {
        return "";
      }
      final StringBuilder contents = new StringBuilder();
      for (Token token : input.tokens(Sets.newTreeSet(Collections.singleton(witnesses.first())))) {
        contents.append(token.getContent()).append(" ");
      }
      return contents.toString().trim();
    }
  };
}
