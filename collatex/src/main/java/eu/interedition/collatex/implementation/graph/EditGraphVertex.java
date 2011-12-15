package eu.interedition.collatex.implementation.graph;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.Iterables;
import eu.interedition.collatex.interfaces.Token;
import org.neo4j.graphdb.Node;

import java.util.Collections;

import static eu.interedition.collatex.implementation.graph.GraphRelationshipType.PATH;
import static org.neo4j.graphdb.Direction.INCOMING;
import static org.neo4j.graphdb.Direction.OUTGOING;

public class EditGraphVertex extends GraphVertex<EditGraph> {

  private static final String BASE_KEY = "base";
  private static final String WITNESS_KEY = "witness";
  private static final String WITNESS_INDEX_KEY = "witnessIndex";

  public EditGraphVertex(EditGraph graph, Node node) {
    super(graph, node);
  }

  public EditGraphVertex(EditGraph graph, Token base, Token witness, int witnessIndex) {
    super(graph, graph.getDatabase().createNode());
    setBase(base);
    setWitness(witness);
    setWitnessIndex(witnessIndex);
  }

  public Token getBase() {
    return getToken(BASE_KEY);    
  }
  
  public void setBase(Token token) {
    setToken(BASE_KEY, token);
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
    return (tokenRef == null ? null : Iterables.getFirst(graph.getTokenResolver().resolve(tokenRef), null));
  }

  protected void setToken(String key, Token base) {
    final Integer tokenRef = base == null ? null : graph.getTokenResolver().resolve(Collections.singleton(base))[0];
    if (tokenRef == null) {
      node.removeProperty(key);
    } else {
      node.setProperty(key, tokenRef);
    }
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
