package eu.interedition.collatex.implementation.graph.db;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import eu.interedition.collatex.interfaces.INormalizedToken;
import eu.interedition.collatex.interfaces.IWitness;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.traversal.Evaluation;
import org.neo4j.graphdb.traversal.Evaluator;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.kernel.Traversal;

import java.util.Map;

import static eu.interedition.collatex.implementation.graph.db.VariantGraphRelationshipType.PATH;
import static org.neo4j.graphdb.Direction.INCOMING;
import static org.neo4j.graphdb.Direction.OUTGOING;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class PersistentVariantGraph {

  private final GraphDatabaseService db;
  private final PersistentVariantGraphVertex start;
  private final PersistentVariantGraphVertex end;
  private final Resolver<IWitness> witnessResolver;
  private final Resolver<INormalizedToken> tokenResolver;

  public PersistentVariantGraph(Node start, Node end, Resolver<IWitness> witnessResolver, Resolver<INormalizedToken> tokenResolver) {
    this.db = start.getGraphDatabase();
    this.start = new PersistentVariantGraphVertex(this, start);
    this.end = new PersistentVariantGraphVertex(this, end);
    this.witnessResolver = witnessResolver;
    this.tokenResolver = tokenResolver;
  }

  public Resolver<IWitness> getWitnessResolver() {
    return witnessResolver;
  }

  public Resolver<INormalizedToken> getTokenResolver() {
    return tokenResolver;
  }

  @Override
  public int hashCode() {
    return start.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj != null && obj instanceof PersistentVariantGraph) {
      return start.equals(((PersistentVariantGraph) obj).start);
    }
    return super.equals(obj);
  }

  private static final TraversalDescription TOPOLOGICAL_SORT_TRAVERSAL = Traversal//
          .description()//
          .relationships(PATH, OUTGOING)//
          .breadthFirst()//
          .evaluator(new Evaluator() {
            private Map<Long, Integer> inDegrees = Maps.newHashMap();

            @Override
            public Evaluation evaluate(Path path) {
              final Node node = path.endNode();
              final int numOtherIncoming = Iterables.size(node.getRelationships(PATH, INCOMING)) - 1;
              if (numOtherIncoming == 0) {
                return Evaluation.INCLUDE_AND_CONTINUE;
              }

              final long nodeId = node.getId();
              final Integer countDown = inDegrees.remove(nodeId);
              if (countDown == null) {
                inDegrees.put(nodeId, numOtherIncoming);
                return Evaluation.EXCLUDE_AND_PRUNE;
              } else if (numOtherIncoming == 0) {
                return Evaluation.INCLUDE_AND_CONTINUE;
              } else {
                inDegrees.put(nodeId, numOtherIncoming - 1);
                return Evaluation.EXCLUDE_AND_PRUNE;
              }
            }
          });

}
