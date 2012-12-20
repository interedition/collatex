package eu.interedition.collatex.neo4j;

import static com.google.common.collect.Iterables.transform;
import static eu.interedition.collatex.neo4j.Neo4jGraphRelationships.PATH;
import static java.util.Collections.singleton;
import static org.neo4j.graphdb.Direction.OUTGOING;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.util.VariantGraphs;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.traversal.Evaluation;
import org.neo4j.graphdb.traversal.Evaluator;
import org.neo4j.kernel.Traversal;
import org.neo4j.kernel.Uniqueness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.collect.RowSortedTable;
import com.google.common.collect.Sets;
import com.google.common.collect.TreeBasedTable;

import eu.interedition.collatex.Token;
import eu.interedition.collatex.Witness;
import eu.interedition.collatex.simple.SimpleToken;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class Neo4jVariantGraph implements VariantGraph {
  private static final Logger LOG = LoggerFactory.getLogger(Neo4jVariantGraph.class);

  final GraphDatabaseService database;
  final EntityMapper<Witness> witnessMapper;
  final EntityMapper<Token> tokenMapper;

  final Neo4jVariantGraphVertex start;
  final Neo4jVariantGraphVertex end;

  public Neo4jVariantGraph(GraphDatabaseService database, Node start, Node end, EntityMapper<Witness> witnessMapper, EntityMapper<Token> tokenMapper) {
    this.database = database;
    this.witnessMapper = witnessMapper;
    this.tokenMapper = tokenMapper;
    this.start = (Neo4jVariantGraphVertex) vertexWrapper.apply(start);
    this.end = (Neo4jVariantGraphVertex) vertexWrapper.apply(end);
  }

  public Transaction newTransaction() {
    return database.beginTx();
  }

  public GraphDatabaseService getDatabase() {
    return database;
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
  public Iterable<Vertex> vertices(final Set<Witness> witnesses) {
    return VariantGraphs.vertices(this, witnesses);
  }

  @Override
  public Iterable<Edge> edges() {
    return edges(null);
  }

  @Override
  public Iterable<Edge> edges(final Set<Witness> witnesses) {
    return VariantGraphs.edges(this, witnesses);
  }

  @Override
  public Neo4jVariantGraphVertex add(Token token) {
    if (LOG.isTraceEnabled()) {
      LOG.trace("Creating new vertex with {}", token);
    }
    return new Neo4jVariantGraphVertex(this, singleton(token));
  }

  @Override
  public Edge connect(VariantGraph.Vertex from, VariantGraph.Vertex to, Set<Witness> witnesses) {
    Preconditions.checkArgument(!from.equals(to));

    if (LOG.isTraceEnabled()) {
      LOG.trace("Connected {} and {} with {}", new Object[]{from, to, witnesses});
    }

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
    return new Neo4jVariantGraphEdge(this, (Neo4jVariantGraphVertex) from, (Neo4jVariantGraphVertex) to, witnesses);
  }

  @Override
  public Transposition transpose(VariantGraph.Vertex from, VariantGraph.Vertex to, int transpId) {
    Preconditions.checkArgument(!from.equals(to));
    Preconditions.checkArgument(!from.tokens().isEmpty());
    Preconditions.checkArgument(!to.tokens().isEmpty());

    //    updateTranspositionIds(from, to);
    for (Transposition t : from.transpositions()) {
      if (t.other(from).equals(to)) {
        return t;
      }
    }

    return new Neo4jVariantGraphTransposition(this, (Neo4jVariantGraphVertex) from, (Neo4jVariantGraphVertex) to, transpId);
  }

  @Override
  public boolean isNear(Vertex a, Vertex b) {
    return verticesAreAdjacent(a, b) && (Iterables.size(a.outgoing()) == 1 || Iterables.size(b.incoming()) == 1);
  }

  @Override
  public boolean verticesAreAdjacent(Vertex a, Vertex b) {
    return (edgeBetween(a, b) != null);
  }

  @Override
  public Edge edgeBetween(Vertex a, Vertex b) {
    final Node aNode = ((Neo4jVariantGraphVertex)a).getNode();
    final Node bNode = ((Neo4jVariantGraphVertex)b).getNode();
    for (Relationship r : aNode.getRelationships(PATH)) {
      if (r.getOtherNode(aNode).equals(bNode)) {
        return new Neo4jVariantGraphEdge(this, r);
      }
    }
    return null;
  }

  @Override
  public Set<Witness> witnesses() {
    final Set<Witness> witnesses = Sets.newHashSet();
    for (Edge e : start.outgoing()) {
      witnesses.addAll(e.witnesses());
    }
    return witnesses;
  }

  @Override
  public VariantGraph join() {
    final Set<Long> processed = Sets.newHashSet();

    final Deque<Vertex> queue = new ArrayDeque<Vertex>();
    for (Edge startingEdges : start.outgoing()) {
      queue.push(startingEdges.to());
    }

    while (!queue.isEmpty()) {
      final Neo4jVariantGraphVertex vertex = (Neo4jVariantGraphVertex) queue.pop();
      Set<Integer> transpositionIds1 = vertex.getTranspositionIds();
      final List<Edge> outgoingEdges = Lists.newArrayList(vertex.outgoing());
      if (outgoingEdges.size() == 1) {
        final Edge joinCandidateEdge = outgoingEdges.get(0);
        final VariantGraph.Vertex joinCandidateVertex = joinCandidateEdge.to();
        Set<Token> candidateTokens = joinCandidateVertex.tokens();
        Set<Integer> transpositionIds2 = ((Neo4jVariantGraphVertex)joinCandidateVertex).getTranspositionIds();

        boolean canJoin = !end.equals(joinCandidateVertex) && //
                Iterables.size(joinCandidateVertex.incoming()) == 1 && //
                transpositionIds1.equals(transpositionIds2);
        if (canJoin) {
          vertex.add(candidateTokens);
          for (Transposition t : joinCandidateVertex.transpositions()) {
            final VariantGraph.Vertex other = t.other(joinCandidateVertex);
            int id = t.getId();
            t.delete();
            transpose(vertex, other, id);
          }
          for (Edge e : Lists.newArrayList(joinCandidateVertex.outgoing())) {
            final VariantGraph.Vertex to = e.to();
            final Set<Witness> witnesses = e.witnesses();
            e.delete();
            connect(vertex, to, witnesses);
          }
          joinCandidateEdge.delete();
          joinCandidateVertex.delete();
          queue.push(vertex);
          continue;
        }
      }

      processed.add(vertex.getNode().getId());
      for (Edge e : outgoingEdges) {
        final VariantGraph.Vertex next = e.to();
        // FIXME: Why do we run out of memory in some cases here, if this is not checked?
        if (!processed.contains(((Neo4jVariantGraphVertex)next).getNode().getId())) {
          queue.push(next);
        }
      }
    }

    return this;
  }

  @Override
  public VariantGraph rank() {
    for (Vertex v : vertices()) {
      int rank = -1;
      for (Edge e : v.incoming()) {
        rank = Math.max(rank, e.from().getRank());
      }
      v.setRank(rank + 1);
    }
    return this;
  }

  @Override
  public VariantGraph adjustRanksForTranspositions() {
    for (Vertex v : vertices()) {
      Iterable<Transposition> transpositions = v.transpositions();
      for (Transposition vgt : transpositions) {
        Vertex from = vgt.from();
        Vertex to = vgt.to();
        if (from.equals(v)) {
          addNullVertex(v, from, to);
        } else if (to.equals(v)) {
          addNullVertex(v, to, from);
        }
      }
    }
    return this;
  }

  private void addNullVertex(Vertex v, Vertex from, Vertex to) {
    Set<Token> nullTokens = Sets.newHashSet();
    for (Witness w : to.witnesses()) {
      nullTokens.add(new SimpleToken(w, -1, "", ""));
    }
    Vertex nullVertex = new Neo4jVariantGraphVertex(this, nullTokens);
    int rank = v.getRank();
    nullVertex.setRank(rank);
    v.setRank(rank + 1);
    for (Vertex ov : vertices()) {
      if (!ov.equals(v) && ov.getRank() > rank) ov.setRank(ov.getRank() + 1);
    }
  }

  @Override
  public Iterable<Set<Vertex>> ranks() {
    return ranks(null);
  }

  @Override
  public Iterable<Set<Vertex>> ranks(final Set<Witness> witnesses) {
    return new Iterable<Set<Vertex>>() {
      @Override
      public Iterator<Set<Vertex>> iterator() {
        return new AbstractIterator<Set<Vertex>>() {
          private final Iterator<Vertex> vertices = vertices(witnesses).iterator();
          private Vertex last;

          @Override
          protected Set<Vertex> computeNext() {
            if (last == null) {
              Preconditions.checkState(vertices.hasNext());
              vertices.next(); // skip start vertex
              Preconditions.checkState(vertices.hasNext());
              last = vertices.next();
            }

            if (last.equals(end)) {
              return endOfData();
            }

            final Set<Vertex> next = Sets.newHashSet();
            next.add(last);

            while (vertices.hasNext()) {
              final Vertex vertex = vertices.next();
              if (vertex.getRank() == last.getRank()) {
                next.add(last = vertex);
              } else {
                last = vertex;
                break;
              }
            }

            return next;
          }
        };
      }
    };
  }

  @Override
  public RowSortedTable<Integer, Witness, Set<Token>> toTable() {
    final TreeBasedTable<Integer, Witness, Set<Token>> table = TreeBasedTable.create(Ordering.natural(), Witness.SIGIL_COMPARATOR);
    for (Vertex v : rank().vertices()) {
      final int row = v.getRank();
      for (Token token : v.tokens()) {
        final Witness column = token.getWitness();

        Set<Token> cell = table.get(row, column);
        if (cell == null) {
          table.put(row, column, cell = Sets.newHashSet());
        }
        cell.add(token);
      }
    }
    return table;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj != null && obj instanceof Neo4jVariantGraph) {
      return start.equals(((Neo4jVariantGraph) obj).start);
    }
    return super.equals(obj);
  }

  @Override
  public int hashCode() {
    return start.hashCode();
  }

  @Override
  public String toString() {
    return Iterables.toString(witnesses());
  }

  final Function<Node, Vertex> vertexWrapper = new Function<Node, VariantGraph.Vertex>() {
    @Override
    public VariantGraph.Vertex apply(Node input) {
      return new Neo4jVariantGraphVertex(Neo4jVariantGraph.this, input);
    }
  };

  final Function<Relationship, Edge> edgeWrapper = new Function<Relationship, VariantGraph.Edge>() {
    @Override
    public VariantGraph.Edge apply(Relationship input) {
      return new Neo4jVariantGraphEdge(Neo4jVariantGraph.this, input);
    }
  };

  final Function<Relationship, Transposition> transpositionWrapper = new Function<Relationship, VariantGraph.Transposition>() {
    @Override
    public VariantGraph.Transposition apply(Relationship input) {
      return new Neo4jVariantGraphTransposition(Neo4jVariantGraph.this, input);
    }
  };
}
