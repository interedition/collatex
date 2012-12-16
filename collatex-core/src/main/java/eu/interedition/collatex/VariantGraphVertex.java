package eu.interedition.collatex;

import eu.interedition.collatex.neo4j.Neo4jVariantGraphVertex;

import java.util.Set;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public interface VariantGraphVertex {
  Iterable<VariantGraphEdge> incoming();

  Iterable<VariantGraphEdge> incoming(Set<Witness> witnesses);

  Iterable<VariantGraphEdge> outgoing();

  Iterable<VariantGraphEdge> outgoing(Set<Witness> witnesses);

  Iterable<VariantGraphTransposition> transpositions();

  Iterable<VariantGraphVertex> vertices(Neo4jVariantGraphVertex to);

  Set<Token> tokens();

  Set<Token> tokens(Set<Witness> witnesses);

  Set<Witness> witnesses();

  void add(Iterable<Token> tokens);

  void setTokens(Set<Token> tokens);

  int getRank();

  void setRank(int rank);

  VariantGraph getGraph();

  void delete();
}
