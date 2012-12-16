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
  private static final String TOKEN_REFERENCE_KEY = "tokenReferences";
  private static final String RANK_KEY = "rank";
  protected final Neo4jVariantGraph graph;
  protected final Node node;

  public Neo4jVariantGraphVertex(Neo4jVariantGraph graph, Node node) {
    this.graph = graph;
    this.node = node;
  }

  public Neo4jVariantGraphVertex(Neo4jVariantGraph graph, Set<Token> tokens) {
    this(graph, graph.getDatabase().createNode());
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
    return transform(node.getRelationships(Neo4jGraphRelationships.TRANSPOSITION), graph.transpositionWrapper);
  }

  @Override
  public Iterable<VariantGraph.Vertex> vertices(final Neo4jVariantGraphVertex to) {
    final int[] witnesses = graph.witnessMapper.map(witnesses());
    return Iterables.transform(Traversal.description().breadthFirst().relationships(Neo4jGraphRelationships.PATH, Direction.OUTGOING).evaluator(new Evaluator() {
      @Override
      public Evaluation evaluate(Path path) {
        final Relationship lastRel = path.lastRelationship();
        if (lastRel != null) {
          if (!((Neo4jVariantGraphEdge) graph.edgeWrapper.apply(lastRel)).traversableWith(witnesses)) {
            return Evaluation.EXCLUDE_AND_PRUNE;
          }
        }
        final Node node = path.endNode();
        if (node != null && node.equals(to.node)) {
          return Evaluation.INCLUDE_AND_PRUNE;
        }

        return Evaluation.INCLUDE_AND_CONTINUE;
      }
    }).traverse(node).nodes(), graph.vertexWrapper);
  }

  ;

  @Override
  public Set<Token> tokens() {
    return tokens(null);
  }

  @Override
  public Set<Token> tokens(Set<Witness> witnesses) {
    final Set<Token> tokens = graph.tokenMapper.map(getTokenReferences());
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

  @Override
  public void setTokens(Set<Token> tokens) {
    setTokenReferences(graph.tokenMapper.map(tokens));
  }

  @Override
  public int getRank() {
    return (Integer) node.getProperty(RANK_KEY);
  }

  @Override
  public void setRank(int rank) {
    node.setProperty(RANK_KEY, rank);
  }

  public int[] getTokenReferences() {
    return (int[]) node.getProperty(TOKEN_REFERENCE_KEY);
  }

  public void setTokenReferences(int... references) {
    node.setProperty(TOKEN_REFERENCE_KEY, references);
  }

  @Override
  public String toString() {
    return Iterables.toString(tokens());
  }

  public static final Function<VariantGraph.Vertex, String> TO_CONTENTS = new Function<VariantGraph.Vertex, String>() {
    @Override
    public String apply(VariantGraph.Vertex input) {
      final Set<Witness> witnesses = input.witnesses();
      if (witnesses.isEmpty()) {
        return "";
      }
      final StringBuilder contents = new StringBuilder();
      for (SimpleToken token : Ordering.natural().sortedCopy(Iterables.filter(input.tokens(singleton(getFirst(witnesses, null))), SimpleToken.class))) {
        contents.append(token.getContent()).append(" ");
      }
      return contents.toString().trim();
    }
  };

  public static final Function<VariantGraph.Vertex, Integer> TO_RANK = new Function<VariantGraph.Vertex, Integer>() {
    @Override
    public Integer apply(VariantGraph.Vertex input) {
      return input.getRank();
    }
  };

  private static final Function<VariantGraph.Transposition, Integer> TRANSPOSITION_ID = new Function<VariantGraph.Transposition, Integer>() {
    @Override
    public Integer apply(@Nullable VariantGraph.Transposition t) {
      return t.getId();
    }
  };

  public Set<Integer> getTranspositionIds() {
    return Sets.newHashSet(Iterables.transform(transpositions(), TRANSPOSITION_ID));
  }

  @Override
  public VariantGraph getGraph() {
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
    if (obj != null && obj instanceof VariantGraph.Vertex) {
      return node.equals(((Neo4jVariantGraphVertex) obj).node);
    }
    return super.equals(obj);
  }
}
