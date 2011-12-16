package eu.interedition.collatex.implementation.graph;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import eu.interedition.collatex.implementation.input.SimpleToken;
import eu.interedition.collatex.implementation.matching.Matches;
import eu.interedition.collatex.interfaces.IWitness;
import eu.interedition.collatex.interfaces.Token;
import org.neo4j.graphalgo.WeightedPath;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.traversal.Evaluation;
import org.neo4j.graphdb.traversal.Evaluator;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.kernel.Traversal;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import static com.google.common.collect.Iterables.transform;
import static eu.interedition.collatex.implementation.graph.EditOperation.GAP;
import static eu.interedition.collatex.implementation.graph.EditOperation.NO_GAP;
import static eu.interedition.collatex.implementation.graph.GraphRelationshipType.PATH;
import static org.neo4j.graphalgo.GraphAlgoFactory.dijkstra;
import static org.neo4j.graphdb.Direction.OUTGOING;
import static org.neo4j.kernel.StandardExpander.DEFAULT;
import static org.neo4j.kernel.Uniqueness.RELATIONSHIP_GLOBAL;

/**
 * This class is the container class for the Edit Graph.
 * <p/>
 * This is a mutable class that is constructed by an external class, since the construction process is an elaborate one. This class is implemented in a defensive style. We use a weighted DAG to make alignment decisions.
 */
@SuppressWarnings("serial")
public class EditGraph extends Graph<EditGraphVertex, EditGraphEdge> {

  public EditGraph(GraphDatabaseService database, Resolver<IWitness> witnessResolver, Resolver<Token> tokenResolver) {
    super(database, witnessResolver, tokenResolver);
  }

  @Override
  public void init(Function<Node, EditGraphVertex> vertexWrapper, Function<Relationship, EditGraphEdge> edgeWrapper, Node start, Node end) {
    super.init(vertexWrapper, edgeWrapper, start, end);
    
    this.start.setBase(SimpleToken.START);
    this.start.setWitness(SimpleToken.START);
    this.start.setWitnessIndex(-1);
    
    this.end.setBase(SimpleToken.END);
    this.end.setWitness(SimpleToken.END);
    this.end.setWitnessIndex(Integer.MAX_VALUE);
  }

  public Iterable<EditGraphVertex> vertices() {
    return transform(createTraversalDescription().traverse(start.getNode()).nodes(), vertexWrapper);
  }

  public Iterable<EditGraphEdge> edges() {
    return transform(createTraversalDescription().uniqueness(RELATIONSHIP_GLOBAL).traverse(start.getNode()).relationships(), edgeWrapper);
  }

  protected TraversalDescription createTraversalDescription() {
    return Traversal.description().breadthFirst().relationships(PATH, OUTGOING);
  }

  public EditGraph build(IWitness base, IWitness witness, Comparator<Token> comparator) {
    Set<EditGraphVertex> prevVertexSet = Sets.newLinkedHashSet();
    prevVertexSet.add(start);
    // build the decision graph from the matches and the variant graph
    Matches m = Matches.between(base, witness, comparator);
    Set<String> ambiguousNormalized = getAmbiguousNormalizedContent(m);
    Multimap<Token, Token> matches = m.getAll();
    // add for vertices for witness tokens that have a matching base token
    int witnessIndex = 0;
    for (Token witnessToken : witness.getTokens()) {
      Collection<Token> baseTokens = matches.get(witnessToken);
      if (!baseTokens.isEmpty()) {
        final Set<EditGraphVertex> vertexSet = Sets.newLinkedHashSet();
        for (Token baseToken : baseTokens) {
          EditGraphVertex editGraphVertex = new EditGraphVertex(this, baseToken, witnessToken, witnessIndex);
          vertexSet.add(editGraphVertex);

          // TODO: you don't want to always draw an edge
          // TODO: in the case of ngrams in witness and superbase
          // TODO: less edges are needed
          for (EditGraphVertex prevVertex : prevVertexSet) {
            connect(prevVertex, editGraphVertex, base.isNear(prevVertex.getBase(), baseToken) ? NO_GAP : GAP);
          }
        }
        prevVertexSet = vertexSet;
      }
      witnessIndex++;
    }

    end.setWitnessIndex(witnessIndex);

    // add edges to end vertex
    for (EditGraphVertex lastVertex : prevVertexSet) {
      connect(lastVertex, end, base.isNear(lastVertex.getBase(), end.getBase()) ? NO_GAP : GAP);
    }

    score();

    //addSkipVertices(ambiguousNormalized);

    int pathId = 0;
    for (WeightedPath path : dijkstra(DEFAULT.add(PATH, OUTGOING), EditGraphEdge.SCORE_KEY).findAllPaths(start.getNode(), end.getNode())) {
      for (EditGraphEdge edge : transform(path.relationships(), edgeWrapper)) {
        edge.addShortestPathId(pathId);
      }
      pathId++;
    }

    return this;
  }

  protected void score() {
    for (EditGraphEdge e : edges()) {
      int score = (e.to().getWitnessIndex() - e.from().getWitnessIndex()) - 1;
      if (e.getEditOperation() == GAP) {
        score += 1;
      }

      final Iterable<EditGraphEdge> prevEdges = e.from().incoming();
      boolean sequence = true;
      for (EditGraphEdge prev : prevEdges) {
        if (prev.getEditOperation() != e.getEditOperation()) {
          sequence = false;
          break;
        }
      }
      if (!Iterables.isEmpty(prevEdges) && sequence) {
        score -= 1;
      }

      e.setScore(score);
    }
  }

  public Iterable<Iterable<EditGraphEdge>> shortestPaths() {
    int maxId = -1;
    for (EditGraphEdge e : start.outgoing()) {
      final SortedSet<Integer> shortestPathIds = e.getShortestPathIds();
      if (!shortestPathIds.isEmpty()) {
        maxId = Math.max(maxId, shortestPathIds.last());
      }
    }
    
    final int lastPathId = maxId;
    return new Iterable<Iterable<EditGraphEdge>>() {
      @Override
      public Iterator<Iterable<EditGraphEdge>> iterator() {
        return new AbstractIterator<Iterable<EditGraphEdge>>() {
          private int pathId = 0;
          @Override
          protected Iterable<EditGraphEdge> computeNext() {
            if (pathId > lastPathId) {
              return endOfData();
            }
            
            return shortestPath(pathId++);
          }
        };
      }
    };
  }
  
  public Iterable<EditGraphEdge> shortestPath(final int id) {
    return transform(createTraversalDescription().evaluator(new Evaluator() {
      @Override
      public Evaluation evaluate(Path path) {
        final Relationship rel = path.lastRelationship();
        if (rel == null) {
          return Evaluation.INCLUDE_AND_CONTINUE;
        }

        final SortedSet<Integer> pathIds = edgeWrapper.apply(rel).getShortestPathIds();
        return (pathIds.contains(id) ? Evaluation.INCLUDE_AND_CONTINUE : Evaluation.EXCLUDE_AND_PRUNE);
      }
    }).traverse(start.getNode()).relationships(), edgeWrapper);
  }

  public Map<Token, Token> linkedTokens() {
    final Map<Token, Token> linkedTokens = Maps.newLinkedHashMap();
    for (Iterable<EditGraphEdge> shortestPath : shortestPaths()) {
      for (EditGraphEdge e : shortestPath) {
        final EditGraphVertex vertex = e.from();
        if (!vertex.equals(start)) {
          linkedTokens.put(vertex.getWitness(), vertex.getBase());
        }
      }
      break;
    }
    return linkedTokens;
  }

  public EditGraphEdge connect(EditGraphVertex from, EditGraphVertex to, EditOperation operation) {
    Preconditions.checkArgument(!from.equals(to));

    for (EditGraphEdge e : from.outgoing()) {
      if (to.equals(e.to())) {
        throw new IllegalStateException(String.format("%s and %s already connected", from, to));
      }
    }
    return new EditGraphEdge(this, from, to, operation);
  }

  private void addSkipVertices(Set<String> ambiguousNormalized) {
    for (EditGraphVertex vertex : vertices()) {
      Token witnessToken = vertex.getWitness();
      if (witnessToken != null) {
        String normalized = ((SimpleToken) witnessToken).getNormalized();
        if (ambiguousNormalized.contains(normalized)) {
          Set<EditGraphEdge> incomingEdges = Sets.newHashSet(vertex.incoming());
          Set<EditGraphEdge> outgoingEdges = Sets.newHashSet(vertex.outgoing());
//          for (EditGraphEdge incomingEdge : incomingEdges) {
//            for (EditGraphEdge outgoingEdge : outgoingEdges) {
//              connect(incomingEdge.from(), outgoingEdge.to(), EditOperation.GAP, 3);
//            }
//          }
          final EditGraphVertex skipVertex = new EditGraphVertex(null, null, null, 0);
          for (EditGraphEdge incomingEdge : incomingEdges) {
            connect(incomingEdge.from(), skipVertex, GAP).setScore(3);
          }
          for (EditGraphEdge outgoingEdge : outgoingEdges) {
            connect(skipVertex, outgoingEdge.to(), NO_GAP);
          }
        }
      }
    }
  }

  private Set<String> getAmbiguousNormalizedContent(Matches m) {
    Set<Token> ambiguousMatches = m.getAmbiguous();
    Set<String> ambiguousNormalized = Sets.newHashSet();
    for (Token token : ambiguousMatches) {
      ambiguousNormalized.add(((SimpleToken) token).getNormalized());
    }
    return ambiguousNormalized;
  }
}
