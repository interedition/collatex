package eu.interedition.collatex.neo4j;

import static com.google.common.collect.Iterables.transform;
import static eu.interedition.collatex.neo4j.GraphRelationshipType.PATH;
import static java.util.Collections.singleton;
import static org.neo4j.graphdb.Direction.OUTGOING;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

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
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
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
public class Neo4jVariantGraph {
  private static final Logger LOG = LoggerFactory.getLogger(Neo4jVariantGraph.class);
  protected final GraphDatabaseService database;
  protected final EntityMapper<Witness> witnessMapper;
  protected final EntityMapper<Token> tokenMapper;
  protected Function<Node, Neo4jVariantGraphVertex> vertexWrapper;
  protected Function<Relationship, Neo4jVariantGraphEdge> edgeWrapper;
  protected Neo4jVariantGraphVertex start;
  protected Neo4jVariantGraphVertex end;
  Map<Token, Integer> transpositionId = Maps.newHashMap();

  private Function<Relationship, Neo4jVariantGraphTransposition> transpositionWrapper;

  public Neo4jVariantGraph(GraphDatabaseService database, EntityMapper<Witness> witnessMapper, EntityMapper<Token> tokenMapper) {
    this.database = database;
    this.witnessMapper = witnessMapper;
    this.tokenMapper = tokenMapper;
  }

  public void init(Function<Node, Neo4jVariantGraphVertex> vertexWrapper, Function<Relationship, Neo4jVariantGraphEdge> edgeWrapper, Node start, Node end) {
    this.vertexWrapper = vertexWrapper;
    this.edgeWrapper = edgeWrapper;
    this.start = (start == null ? null : vertexWrapper.apply(start));
    this.end = (end == null ? null : vertexWrapper.apply(end));
    this.transpositionWrapper = Neo4jVariantGraphTransposition.createWrapper(this);
  }

  public Transaction newTransaction() {
    return database.beginTx();
  }

  public GraphDatabaseService getDatabase() {
    return database;
  }

  public Neo4jVariantGraphVertex getStart() {
    return start;
  }

  public Neo4jVariantGraphVertex getEnd() {
    return end;
  }

  public EntityMapper<Witness> getWitnessMapper() {
    return witnessMapper;
  }

  public Function<Relationship, Neo4jVariantGraphTransposition> getTranspositionWrapper() {
    return transpositionWrapper;
  }

  public Set<Neo4jVariantGraphTransposition> transpositions() {
    final Set<Neo4jVariantGraphTransposition> transpositions = Sets.newHashSet();
    for (Neo4jVariantGraphVertex v : vertices()) {
      Iterables.addAll(transpositions, v.transpositions());
    }
    return transpositions;
  }

  public Iterable<Neo4jVariantGraphVertex> vertices() {
    return vertices(null);
  }

  public Iterable<Neo4jVariantGraphVertex> vertices(final Set<Witness> witnesses) {
    return new Iterable<Neo4jVariantGraphVertex>() {
      @Override
      public Iterator<Neo4jVariantGraphVertex> iterator() {
        return new AbstractIterator<Neo4jVariantGraphVertex>() {
          private final Map<Long, Integer> encountered = Maps.newHashMap();
          private final Queue<Neo4jVariantGraphVertex> queue = new ArrayDeque<Neo4jVariantGraphVertex>(singleton(getStart()));

          @Override
          protected Neo4jVariantGraphVertex computeNext() {
            if (queue.isEmpty()) {
              return endOfData();
            }
            final Neo4jVariantGraphVertex next = queue.remove();
            for (Neo4jVariantGraphEdge edge : next.outgoing(witnesses)) {
              final Neo4jVariantGraphVertex end = edge.to();
              final long endId = end.getNode().getId();

              final int endEncountered = Objects.firstNonNull(encountered.get(endId), 0);
              final int endIncoming = Iterables.size(end.incoming(witnesses));

              if (endIncoming == endEncountered) {
                throw new IllegalStateException(String.format("Encountered cycle traversing %s to %s", edge, end));
              } else if ((endIncoming - endEncountered) == 1) {
                queue.add(end);
              }

              encountered.put(endId, endEncountered + 1);
            }
            return next;
          }
        };
      }
    };
  }

  public Iterable<Neo4jVariantGraphEdge> edges() {
    return edges(null);
  }

  public Iterable<Neo4jVariantGraphEdge> edges(final Set<Witness> witnesses) {
    final int[] witnessReferences = (witnesses == null || witnesses.isEmpty()) ? null : getWitnessMapper().map(witnesses);
    return transform(Traversal.description().relationships(PATH, OUTGOING).uniqueness(Uniqueness.RELATIONSHIP_GLOBAL).breadthFirst().evaluator(new Evaluator() {

      @Override
      public Evaluation evaluate(Path path) {
        if (witnessReferences != null) {
          final Relationship lastRel = path.lastRelationship();
          if (lastRel != null) {
            if (!edgeWrapper.apply(lastRel).traversableWith(witnessReferences)) {
              return Evaluation.EXCLUDE_AND_PRUNE;
            }
          }
        }

        return Evaluation.INCLUDE_AND_CONTINUE;
      }
    }).traverse(start.getNode()).relationships(), edgeWrapper);
  }

  public Neo4jVariantGraphVertex add(Token token) {
    if (LOG.isTraceEnabled()) {
      LOG.trace("Creating new vertex with {}", token);
    }
    return new Neo4jVariantGraphVertex(this, singleton(token));
  }

  public Neo4jVariantGraphEdge connect(Neo4jVariantGraphVertex from, Neo4jVariantGraphVertex to, Set<Witness> witnesses) {
    Preconditions.checkArgument(!from.equals(to));

    if (LOG.isTraceEnabled()) {
      LOG.trace("Connected {} and {} with {}", new Object[]{from, to, witnesses});
    }

    if (from.equals(start)) {
      final Neo4jVariantGraphEdge startEndEdge = edgeBetween(start, end);
      if (startEndEdge != null) {
        startEndEdge.delete();
      }
    }

    for (Neo4jVariantGraphEdge e : from.outgoing()) {
      if (to.equals(e.to())) {
        return e.add(witnesses);
      }
    }
    return new Neo4jVariantGraphEdge(this, from, to, witnesses);
  }

  public Neo4jVariantGraphTransposition transpose(Neo4jVariantGraphVertex from, Neo4jVariantGraphVertex to, int transpId) {
    Preconditions.checkArgument(!from.equals(to));
    Preconditions.checkArgument(!from.tokens().isEmpty());
    Preconditions.checkArgument(!to.tokens().isEmpty());

    //    updateTranspositionIds(from, to);
    for (Neo4jVariantGraphTransposition t : from.transpositions()) {
      if (t.other(from).equals(to)) {
        return t;
      }
    }

    return new Neo4jVariantGraphTransposition(this, from, to, transpId);
  }

  public boolean isNear(Neo4jVariantGraphVertex a, Neo4jVariantGraphVertex b) {
    return verticesAreAdjacent(a, b) && (Iterables.size(a.outgoing()) == 1 || Iterables.size(b.incoming()) == 1);
  }

  public boolean verticesAreAdjacent(Neo4jVariantGraphVertex a, Neo4jVariantGraphVertex b) {
    return (edgeBetween(a, b) != null);
  }

  public Neo4jVariantGraphEdge edgeBetween(Neo4jVariantGraphVertex a, Neo4jVariantGraphVertex b) {
    final Node aNode = a.getNode();
    final Node bNode = b.getNode();
    for (Relationship r : aNode.getRelationships(PATH)) {
      if (r.getOtherNode(aNode).equals(bNode)) {
        return new Neo4jVariantGraphEdge(this, r);
      }
    }
    return null;
  }

  public Set<Witness> witnesses() {
    final Set<Witness> witnesses = Sets.newHashSet();
    for (Neo4jVariantGraphEdge e : start.outgoing()) {
      witnesses.addAll(e.witnesses());
    }
    return witnesses;
  }

  public Neo4jVariantGraph join() {
    final Set<Long> processed = Sets.newHashSet();

    final Deque<Neo4jVariantGraphVertex> queue = new ArrayDeque<Neo4jVariantGraphVertex>();
    for (Neo4jVariantGraphEdge startingEdges : start.outgoing()) {
      queue.push(startingEdges.to());
    }

    while (!queue.isEmpty()) {
      final Neo4jVariantGraphVertex vertex = queue.pop();
      Set<Integer> transpositionIds1 = vertex.getTranspositionIds();
      final List<Neo4jVariantGraphEdge> outgoingEdges = Lists.newArrayList(vertex.outgoing());
      if (outgoingEdges.size() == 1) {
        final Neo4jVariantGraphEdge joinCandidateEdge = outgoingEdges.get(0);
        final Neo4jVariantGraphVertex joinCandidateVertex = joinCandidateEdge.to();
        Set<Token> candidateTokens = joinCandidateVertex.tokens();
        Set<Integer> transpositionIds2 = joinCandidateVertex.getTranspositionIds();

        boolean canJoin = !end.equals(joinCandidateVertex) && //
                Iterables.size(joinCandidateVertex.incoming()) == 1 && //
                transpositionIds1.equals(transpositionIds2);
        if (canJoin) {
          vertex.add(candidateTokens);
          for (Neo4jVariantGraphTransposition t : joinCandidateVertex.transpositions()) {
            final Neo4jVariantGraphVertex other = t.other(joinCandidateVertex);
            int id = t.getId();
            t.delete();
            transpose(vertex, other, id);
          }
          for (Neo4jVariantGraphEdge e : Lists.newArrayList(joinCandidateVertex.outgoing())) {
            final Neo4jVariantGraphVertex to = e.to();
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
      for (Neo4jVariantGraphEdge e : outgoingEdges) {
        final Neo4jVariantGraphVertex next = e.to();
        // FIXME: Why do we run out of memory in some cases here, if this is not checked?
        if (!processed.contains(next.getNode().getId())) {
          queue.push(next);
        }
      }
    }

    return this;
  }

  public Neo4jVariantGraph rank() {
    for (Neo4jVariantGraphVertex v : vertices()) {
      int rank = -1;
      for (Neo4jVariantGraphEdge e : v.incoming()) {
        rank = Math.max(rank, e.from().getRank());
      }
      v.setRank(rank + 1);
    }
    return this;
  }

  public Neo4jVariantGraph adjustRanksForTranspositions() {
    for (Neo4jVariantGraphVertex v : vertices()) {
      Iterable<Neo4jVariantGraphTransposition> transpositions = v.transpositions();
      for (Neo4jVariantGraphTransposition vgt : transpositions) {
        Neo4jVariantGraphVertex from = vgt.from();
        Neo4jVariantGraphVertex to = vgt.to();
        if (from.equals(v)) {
          addNullVertex(v, from, to);
        } else if (to.equals(v)) {
          addNullVertex(v, to, from);
        }
      }
    }
    return this;
  }

  private void addNullVertex(Neo4jVariantGraphVertex v, Neo4jVariantGraphVertex from, Neo4jVariantGraphVertex to) {
    Set<Token> nullTokens = Sets.newHashSet();
    for (Witness w : to.witnesses()) {
      nullTokens.add(new SimpleToken(w, -1, "", ""));
    }
    Neo4jVariantGraphVertex nullVertex = new Neo4jVariantGraphVertex(this, nullTokens);
    int rank = v.getRank();
    nullVertex.setRank(rank);
    v.setRank(rank + 1);
    for (Neo4jVariantGraphVertex ov : vertices()) {
      if (!ov.equals(v) && ov.getRank() > rank) ov.setRank(ov.getRank() + 1);
    }
  }

  public Iterable<Set<Neo4jVariantGraphVertex>> ranks() {
    return ranks(null);
  }

  public Iterable<Set<Neo4jVariantGraphVertex>> ranks(final Set<Witness> witnesses) {
    return new Iterable<Set<Neo4jVariantGraphVertex>>() {
      @Override
      public Iterator<Set<Neo4jVariantGraphVertex>> iterator() {
        return new AbstractIterator<Set<Neo4jVariantGraphVertex>>() {
          private final Iterator<Neo4jVariantGraphVertex> vertices = vertices(witnesses).iterator();
          private Neo4jVariantGraphVertex last;

          @Override
          protected Set<Neo4jVariantGraphVertex> computeNext() {
            if (last == null) {
              Preconditions.checkState(vertices.hasNext());
              vertices.next(); // skip start vertex
              Preconditions.checkState(vertices.hasNext());
              last = vertices.next();
            }

            if (last.equals(end)) {
              return endOfData();
            }

            final Set<Neo4jVariantGraphVertex> next = Sets.newHashSet();
            next.add(last);

            while (vertices.hasNext()) {
              final Neo4jVariantGraphVertex vertex = vertices.next();
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

  public RowSortedTable<Integer, Witness, Set<Token>> toTable() {
    final TreeBasedTable<Integer, Witness, Set<Token>> table = TreeBasedTable.create(Ordering.natural(), Witness.SIGIL_COMPARATOR);
    for (Neo4jVariantGraphVertex v : rank().vertices()) {
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
  public String toString() {
    return Iterables.toString(witnesses());
  }

  public EntityMapper<Token> getTokenMapper() {
    return tokenMapper;
  }

  public Function<Node, Neo4jVariantGraphVertex> getVertexWrapper() {
    return vertexWrapper;
  }

  public Function<Relationship, Neo4jVariantGraphEdge> getEdgeWrapper() {
    return edgeWrapper;
  }

  @Override
  public boolean equals(Object obj) {
    if (start != null && obj != null && obj instanceof Neo4jVariantGraph) {
      return start.equals(((Neo4jVariantGraph) obj).start);
    }
    return super.equals(obj);
  }

  @Override
  public int hashCode() {
    return (start == null ? super.hashCode() : start.hashCode());
  }
}
