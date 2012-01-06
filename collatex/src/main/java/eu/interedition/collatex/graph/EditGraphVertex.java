package eu.interedition.collatex.graph;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import eu.interedition.collatex.Token;
import org.neo4j.graphdb.Node;

import java.util.Collections;

import static eu.interedition.collatex.graph.GraphRelationshipType.PATH;
import static org.neo4j.graphdb.Direction.INCOMING;
import static org.neo4j.graphdb.Direction.OUTGOING;

public class EditGraphVertex extends GraphVertex<EditGraph> {

  private static final String BASE_KEY = "base";
  private static final String WITNESS_KEY = "witness";
  private static final String WITNESS_INDEX_KEY = "witnessIndex";

  public EditGraphVertex(EditGraph graph, Node node) {
    super(graph, node);
  }

  public EditGraphVertex(EditGraph graph, VariantGraphVertex base, Token witness, int witnessIndex) {
    super(graph, graph.getDatabase().createNode());
    setBase(base);
    setWitness(witness);
    setWitnessIndex(witnessIndex);
  }

  public VariantGraphVertex getBase() {
    final Long vertexId = (Long) node.getProperty(BASE_KEY, null);
    return (vertexId == null ? null : graph.getVariantGraphVertexWrapper().apply(graph.getDatabase().getNodeById(vertexId)));
  }
  
  public void setBase(VariantGraphVertex vertex) {
    if (vertex == null) {
      node.removeProperty(BASE_KEY);
    } else {
      node.setProperty(BASE_KEY, vertex.getNode().getId());
    }
  }

  public Token getWitness() {
    return getToken(WITNESS_KEY);
  }

  public void setWitness(Token token) {
    setToken(WITNESS_KEY, token);
  }

  public int getWitnessIndex() {
    return (Integer) node.getProperty(WITNESS_INDEX_KEY);
  }

  public void setWitnessIndex(int witnessIndex) {
    node.setProperty(WITNESS_INDEX_KEY, witnessIndex);
  }

  public Iterable<EditGraphEdge> outgoing() {
    return Iterables.transform(node.getRelationships(PATH, OUTGOING), graph.getEdgeWrapper());
  }

  public Iterable<EditGraphEdge> incoming() {
    return Iterables.transform(node.getRelationships(PATH, INCOMING), graph.getEdgeWrapper());
  }

  protected Token getToken(String key) {
    final Integer tokenRef = (Integer) node.getProperty(key, null);
    return (tokenRef == null ? null : Iterables.getFirst(graph.getTokenMapper().map(tokenRef), null));
  }

  protected void setToken(String key, Token base) {
    final Integer tokenRef = (base == null ? null : graph.getTokenMapper().map(Collections.singleton(base))[0]);
    if (tokenRef == null) {
      node.removeProperty(key);
    } else {
      node.setProperty(key, tokenRef);
    }
  }

  @Override
  public String toString() {
    return new StringBuilder(getWitness().toString()).append(" = ").append(getBase().toString()).toString();
  }

  public static Function<Node, EditGraphVertex> createWrapper(final EditGraph graph) {
    return new Function<Node, EditGraphVertex>() {
      @Override
      public EditGraphVertex apply(Node input) {
        return new EditGraphVertex(graph, input);
      }
    };
  }
}
