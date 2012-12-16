package eu.interedition.collatex;

import com.google.common.collect.RowSortedTable;
import eu.interedition.collatex.neo4j.Neo4jVariantGraph;
import eu.interedition.collatex.neo4j.Neo4jVariantGraphVertex;

import java.util.Set;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public interface VariantGraph {
  Neo4jVariantGraphVertex getStart();

  Neo4jVariantGraphVertex getEnd();

  Set<VariantGraphTransposition> transpositions();

  Iterable<VariantGraphVertex> vertices();

  Iterable<VariantGraphVertex> vertices(Set<Witness> witnesses);

  Iterable<VariantGraphEdge> edges();

  Iterable<VariantGraphEdge> edges(Set<Witness> witnesses);

  Neo4jVariantGraphVertex add(Token token);

  VariantGraphEdge connect(Neo4jVariantGraphVertex from, Neo4jVariantGraphVertex to, Set<Witness> witnesses);

  VariantGraphTransposition transpose(Neo4jVariantGraphVertex from, Neo4jVariantGraphVertex to, int transpId);

  boolean isNear(Neo4jVariantGraphVertex a, Neo4jVariantGraphVertex b);

  boolean verticesAreAdjacent(Neo4jVariantGraphVertex a, Neo4jVariantGraphVertex b);

  VariantGraphEdge edgeBetween(Neo4jVariantGraphVertex a, Neo4jVariantGraphVertex b);

  Set<Witness> witnesses();

  Neo4jVariantGraph join();

  VariantGraph rank();

  VariantGraph adjustRanksForTranspositions();

  Iterable<Set<VariantGraphVertex>> ranks();

  Iterable<Set<VariantGraphVertex>> ranks(Set<Witness> witnesses);

  RowSortedTable<Integer, Witness, Set<Token>> toTable();
}
