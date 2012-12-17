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

  Set<Transposition> transpositions();

  Iterable<Vertex> vertices();

  Iterable<Vertex> vertices(Set<Witness> witnesses);

  Iterable<Edge> edges();

  Iterable<Edge> edges(Set<Witness> witnesses);

  Neo4jVariantGraphVertex add(Token token);

  Edge connect(VariantGraph.Vertex from, VariantGraph.Vertex to, Set<Witness> witnesses);

  Transposition transpose(VariantGraph.Vertex from, VariantGraph.Vertex to, int transpId);

  boolean isNear(Neo4jVariantGraphVertex a, Neo4jVariantGraphVertex b);

  boolean verticesAreAdjacent(Neo4jVariantGraphVertex a, Neo4jVariantGraphVertex b);

  Edge edgeBetween(Neo4jVariantGraphVertex a, Neo4jVariantGraphVertex b);

  Set<Witness> witnesses();

  Neo4jVariantGraph join();

  VariantGraph rank();

  VariantGraph adjustRanksForTranspositions();

  Iterable<Set<Vertex>> ranks();

  Iterable<Set<Vertex>> ranks(Set<Witness> witnesses);

  RowSortedTable<Integer, Witness, Set<Token>> toTable();

  /**
   * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
   */
  interface Edge {
    boolean traversableWith(Set<Witness> witnesses);

    Edge add(Set<Witness> witnesses);

    Set<Witness> witnesses();

    Neo4jVariantGraph getGraph();

    Neo4jVariantGraphVertex from();

    Neo4jVariantGraphVertex to();

    void delete();
  }

  /**
   * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
   */
  interface Vertex {
    Iterable<Edge> incoming();

    Iterable<Edge> incoming(Set<Witness> witnesses);

    Iterable<Edge> outgoing();

    Iterable<Edge> outgoing(Set<Witness> witnesses);

    Iterable<Transposition> transpositions();

    Iterable<Vertex> vertices(Neo4jVariantGraphVertex to);

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

  /**
   * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
   */
  interface Transposition {
    Vertex from();

    Vertex to();

    Vertex other(VariantGraph.Vertex vertex);

    void delete();

    int getId();
  }
}
