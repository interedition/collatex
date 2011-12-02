package eu.interedition.collatex.implementation.graph.db;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import eu.interedition.collatex.interfaces.INormalizedToken;
import eu.interedition.collatex.interfaces.IWitness;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import static com.google.common.collect.Iterables.transform;
import static eu.interedition.collatex.implementation.graph.db.VariantGraphRelationshipType.PATH;
import static org.neo4j.graphdb.Direction.INCOMING;
import static org.neo4j.graphdb.Direction.OUTGOING;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class PersistentVariantGraphVertex {
  private final PersistentVariantGraph graph;
  private final Node node;
  private static final String TOKEN_REFERENCE_KEY = "tokenReferences";
  private static final String RANK_KEY = "rank";

  public PersistentVariantGraphVertex(PersistentVariantGraph graph, Node node) {
    this.graph = graph;
    this.node = node;
  }

  public PersistentVariantGraphVertex(PersistentVariantGraph graph, SortedSet<INormalizedToken> tokens) {
    this(graph, graph.getDb().createNode());
    setTokens(tokens);
  }

  public PersistentVariantGraph getGraph() {
    return graph;
  }

  public Node getNode() {
    return node;
  }

  public SortedSet<INormalizedToken> getTokens(SortedSet<IWitness> witnesses) {
    final SortedSet<INormalizedToken> tokens = Sets.newTreeSet(graph.getTokenResolver().resolve(getTokenReferences()));
    if (witnesses != null && !witnesses.isEmpty()) {
      for (Iterator<INormalizedToken> tokenIt = tokens.iterator(); tokenIt.hasNext(); ) {
        final INormalizedToken token = tokenIt.next();
        if (!witnesses.contains(token.getWitness())) {
          tokenIt.remove();
        }
      }
    }
    return tokens;
  }

  public void add(Iterable<INormalizedToken> tokens) {
    final SortedSet<INormalizedToken> tokenSet = Sets.newTreeSet(getTokens(null));
    Iterables.addAll(tokenSet, tokens);
    setTokens(tokenSet);
  }

  public void setTokens(SortedSet<INormalizedToken> tokens) {
    setTokenReferences(graph.getTokenResolver().resolve(tokens));
  }

  public Iterable<PersistentVariantGraphEdge> getIncomingPaths() {
    return transform(node.getRelationships(PATH, INCOMING), graph.getEdgeWrapper());
  }

  public Iterable<PersistentVariantGraphEdge> getOutgoingPaths() {
    return transform(node.getRelationships(PATH, OUTGOING), graph.getEdgeWrapper());
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

  public SortedSet<IWitness> getWitnesses() {
    final SortedSet<IWitness> witnesses = Sets.newTreeSet();
    for (INormalizedToken token : getTokens(null)) {
      witnesses.add(token.getWitness());
    }
    return witnesses;
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
    if (obj != null && obj instanceof PersistentVariantGraphVertex) {
      return node.equals(((PersistentVariantGraphVertex)obj).node);
    }
    return super.equals(obj);
  }

  public static Function<Node, PersistentVariantGraphVertex> createWrapper(final PersistentVariantGraph in) {
    return new Function<Node, PersistentVariantGraphVertex>() {
      @Override
      public PersistentVariantGraphVertex apply(Node input) {
        return new PersistentVariantGraphVertex(in, input);
      }
    };
  }
}
