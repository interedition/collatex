package eu.interedition.collatex.neo4j;

import eu.interedition.collatex.Token;
import eu.interedition.collatex.Witness;

import java.util.Set;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public interface Neo4jVariantGraphAdapter {

  Set<Token> getTokens(Neo4jVariantGraphVertex vertex, Set<Witness> witnesses);

  void setTokens(Neo4jVariantGraphVertex vertex, Set<Token> tokens);

  Set<Witness> getWitnesses(Neo4jVariantGraphEdge edge);

  void setWitnesses(Neo4jVariantGraphEdge edge, Set<Witness> witnesses);

}
