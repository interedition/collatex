package eu.interedition.collatex.graph;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.common.collect.RowSortedTable;
import com.google.common.collect.Sets;
import com.google.common.collect.TreeBasedTable;
import eu.interedition.collatex.Witness;
import eu.interedition.collatex.Token;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.traversal.Evaluation;
import org.neo4j.graphdb.traversal.Evaluator;
import org.neo4j.kernel.Traversal;
import org.neo4j.kernel.Uniqueness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import static com.google.common.collect.Iterables.transform;
import static eu.interedition.collatex.graph.GraphRelationshipType.PATH;
import static java.util.Collections.singleton;
import static org.neo4j.graphdb.Direction.OUTGOING;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class VariantGraph extends Graph<VariantGraphVertex, VariantGraphEdge> {
  private static final Logger LOG = LoggerFactory.getLogger(VariantGraph.class);

  private Function<Relationship, VariantGraphTransposition> transpositionWrapper;

  public VariantGraph(GraphDatabaseService database, Resolver<Witness> witnessResolver, Resolver<Token> tokenResolver) {
    super(database, witnessResolver, tokenResolver);
  }

  @Override
  public void init(Function<Node, VariantGraphVertex> vertexWrapper, Function<Relationship, VariantGraphEdge> edgeWrapper, Node start, Node end) {
    super.init(vertexWrapper, edgeWrapper, start, end);
    this.transpositionWrapper = VariantGraphTransposition.createWrapper(this);
  }

  public Function<Relationship, VariantGraphTransposition> getTranspositionWrapper() {
    return transpositionWrapper;
  }

  public Set<VariantGraphTransposition> transpositions() {
    final Set<VariantGraphTransposition> transpositions = Sets.newHashSet();
    for (VariantGraphVertex v : vertices()) {
      Iterables.addAll(transpositions, v.transpositions());
    }
    return transpositions;
  }

  public Iterable<VariantGraphVertex> vertices() {
    return vertices(null);
  }

  public Iterable<VariantGraphVertex> vertices(final Set<Witness> witnesses) {
    return new Iterable<VariantGraphVertex>() {
      @Override
      public Iterator<VariantGraphVertex> iterator() {
        return new AbstractIterator<VariantGraphVertex>() {
          private Map<VariantGraphVertex, Integer> encountered = Maps.newHashMap();
          private Queue<VariantGraphVertex> queue = new ArrayDeque<VariantGraphVertex>(singleton(getStart()));

          @Override
          protected VariantGraphVertex computeNext() {
            if (queue.isEmpty()) {
              return endOfData();
            }
            final VariantGraphVertex next = queue.remove();
            for (VariantGraphEdge edge : next.outgoing(witnesses)) {
              final VariantGraphVertex end = edge.to();
              final int endIncoming = Iterables.size(end.incoming(witnesses));
              if (endIncoming == 1) {
                queue.add(end);
              } else if (encountered.containsKey(end)) {
                final int endEncountered = encountered.remove(end);
                if ((endIncoming - endEncountered) == 1) {
                  queue.add(end);
                } else {
                  encountered.put(end, endEncountered + 1);
                }
              } else {
                encountered.put(end, 1);
              }
            }
            return next;
          }
        };
      }
    };
  }

  public Iterable<VariantGraphEdge> edges() {
    return edges(null);
  }

  public Iterable<VariantGraphEdge> edges(final Set<Witness> witnesses) {
    return transform(Traversal.description().relationships(PATH, OUTGOING).uniqueness(Uniqueness.RELATIONSHIP_GLOBAL).breadthFirst().evaluator(new Evaluator() {

      @Override
      public Evaluation evaluate(Path path) {
        if (witnesses != null && !witnesses.isEmpty()) {
          final Relationship lastRel = path.lastRelationship();
          if (lastRel != null) {
            if (edgeWrapper.apply(lastRel).traversableWith(witnesses)) {
              return Evaluation.EXCLUDE_AND_PRUNE;
            }
          }
        }

        return Evaluation.INCLUDE_AND_CONTINUE;
      }
    }).traverse(start.getNode()).relationships(), edgeWrapper);
  }

  public VariantGraphVertex add(Token token) {
    if (LOG.isTraceEnabled()) {
      LOG.trace("Creating new vertex with {}", token);
    }
    return new VariantGraphVertex(this, singleton(token));
  }

  public VariantGraphEdge connect(VariantGraphVertex from, VariantGraphVertex to, Set<Witness> witnesses) {
    Preconditions.checkArgument(!from.equals(to));

    if (LOG.isTraceEnabled()) {
      LOG.trace("Connected {} and {} with {}", new Object[] { from, to, witnesses });
    }

    if (from.equals(start)) {
      final VariantGraphEdge startEndEdge = edgeBetween(start, end);
      if (startEndEdge != null) {
        startEndEdge.delete();
      }
    }

    for (VariantGraphEdge e : from.outgoing()) {
      if (to.equals(e.to())) {
        return e.add(witnesses);
      }
    }
    return new VariantGraphEdge(this, from, to, witnesses);
  }

  public VariantGraphTransposition transpose(VariantGraphVertex from, VariantGraphVertex to) {
    Preconditions.checkArgument(!from.equals(to));
    Preconditions.checkArgument(!from.tokens().isEmpty());
    Preconditions.checkArgument(!to.tokens().isEmpty());

    for (VariantGraphTransposition t : from.transpositions()) {
      if (t.other(from).equals(to)) {
        return t;
      }
    }
    
    return new VariantGraphTransposition(this, from, to);
  }

  public boolean isNear(VariantGraphVertex a, VariantGraphVertex b) {
    return verticesAreAdjacent(a, b) && (Iterables.size(a.outgoing()) == 1 || Iterables.size(b.incoming()) == 1);
  }

  public boolean verticesAreAdjacent(VariantGraphVertex a, VariantGraphVertex b) {
    return (edgeBetween(a, b) != null);
  }

  public VariantGraphEdge edgeBetween(VariantGraphVertex a, VariantGraphVertex b) {
    final Node aNode = a.getNode();
    final Node bNode = b.getNode();
    for (Relationship r : aNode.getRelationships(PATH)) {
      if (r.getOtherNode(aNode).equals(bNode)) {
        return new VariantGraphEdge(this, r);
      }
    }
    return null;
  }

  public Set<Witness> witnesses() {
    final Set<Witness> witnesses = Sets.newHashSet();
    for (VariantGraphEdge e : start.outgoing()) {
      witnesses.addAll(e.witnesses());
    }
    return witnesses;
  }

  public VariantGraph join() {
    final Queue<VariantGraphVertex> queue = new ArrayDeque<VariantGraphVertex>();
    for (VariantGraphEdge startingEdges : start.outgoing()) {
      queue.add(startingEdges.to());
    }

    while (!queue.isEmpty()) {
      final VariantGraphVertex vertex = queue.remove();
      final List<VariantGraphEdge> outgoingEdges = Lists.newArrayList(vertex.outgoing());
      if (outgoingEdges.size() == 1) {
        final VariantGraphEdge joinCandidateEdge = outgoingEdges.get(0);
        final VariantGraphVertex joinCandidateVertex = joinCandidateEdge.to();
        if (Iterables.size(joinCandidateVertex.incoming()) == 1) {
          final Set<Witness> incomingWitnesses = joinCandidateEdge.witnesses();
          final Set<Witness> outgoingWitnesses = Sets.newHashSet();
          final List<VariantGraphEdge> joinCandidateOutgoing = Lists.newArrayList(joinCandidateVertex.outgoing());
          for (VariantGraphEdge e : joinCandidateOutgoing) {
            outgoingWitnesses.addAll(e.witnesses());
          }
          if (incomingWitnesses.equals(outgoingWitnesses)) {
            vertex.add(joinCandidateVertex.tokens());
            for (VariantGraphTransposition t : joinCandidateVertex.transpositions()) {
              transpose(vertex, t.other(joinCandidateVertex));
              t.delete();
            }
            for (VariantGraphEdge e : joinCandidateOutgoing) {
              connect(vertex, e.to(), e.witnesses());
              e.delete();
            }
            joinCandidateEdge.delete();
            joinCandidateVertex.delete();

            outgoingEdges.remove(joinCandidateEdge);
            queue.add(vertex);
          }
        }
      }
      for (VariantGraphEdge e : outgoingEdges) {
        queue.add(e.to());
      }
    }

    return this;
  }

  public VariantGraph rank() {
    for (VariantGraphVertex v : vertices()) {
      int rank = -1;
      for (VariantGraphEdge e : v.incoming()) {
        rank = Math.max(rank, e.from().getRank());
      }
      v.setRank(rank + 1);
    }

    return this;
  }

  public Iterable<Set<VariantGraphVertex>> ranks() {
    return ranks(null);
  }

  public Iterable<Set<VariantGraphVertex>> ranks(final Set<Witness> witnesses) {
    return new Iterable<Set<VariantGraphVertex>>() {
      @Override
      public Iterator<Set<VariantGraphVertex>> iterator() {
        return new AbstractIterator<Set<VariantGraphVertex>>() {
          private Iterator<VariantGraphVertex> vertices = vertices(witnesses).iterator();
          private VariantGraphVertex last;
          
          @Override
          protected Set<VariantGraphVertex> computeNext() {
            if (last == null) {
              Preconditions.checkState(vertices.hasNext());
              vertices.next(); // skip start vertex
              Preconditions.checkState(vertices.hasNext());
              last = vertices.next();
            }

            if (last.equals(end)) {
              return endOfData();
            }

            final Set<VariantGraphVertex> next = Sets.newHashSet();
            next.add(last);

            while (vertices.hasNext()) {
              final VariantGraphVertex vertex = vertices.next();
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
    for (VariantGraphVertex v : rank().vertices()) {
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
}
