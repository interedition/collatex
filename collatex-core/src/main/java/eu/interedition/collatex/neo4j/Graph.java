package eu.interedition.collatex.neo4j;

import com.google.common.base.Function;
import eu.interedition.collatex.Witness;
import eu.interedition.collatex.Token;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public abstract class Graph<V extends GraphVertex, E extends GraphEdge> {
  protected final GraphDatabaseService database;
  protected final EntityMapper<Witness> witnessMapper;
  protected final EntityMapper<Token> tokenMapper;

  protected Function<Node, V> vertexWrapper;
  protected Function<Relationship, E> edgeWrapper;
  protected V start;
  protected V end;

  public Graph(GraphDatabaseService database, EntityMapper<Witness> witnessMapper, EntityMapper<Token> tokenMapper) {
    this.database = database;
    this.witnessMapper = witnessMapper;
    this.tokenMapper = tokenMapper;
  }

  public void init(Function<Node, V> vertexWrapper, Function<Relationship, E> edgeWrapper, Node start, Node end) {
    this.vertexWrapper = vertexWrapper;
    this.edgeWrapper = edgeWrapper;
    this.start = (start == null ? null : vertexWrapper.apply(start));
    this.end = (end == null ? null : vertexWrapper.apply(end));
  }

  public Transaction newTransaction() {
    return database.beginTx();
  }

  public GraphDatabaseService getDatabase() {
    return database;
  }

  public V getStart() {
    return start;
  }

  public V getEnd() {
    return end;
  }

  public EntityMapper<Witness> getWitnessMapper() {
    return witnessMapper;
  }

  public EntityMapper<Token> getTokenMapper() {
    return tokenMapper;
  }

  public Function<Node, V> getVertexWrapper() {
    return vertexWrapper;
  }

  public Function<Relationship, E> getEdgeWrapper() {
    return edgeWrapper;
  }

  @Override
  public boolean equals(Object obj) {
    if (start != null && obj != null && obj instanceof Graph) {
      return start.equals(((Graph) obj).start);
    }
    return super.equals(obj);
  }

  @Override
  public int hashCode() {
    return (start == null ? super.hashCode() : start.hashCode());
  }
}
