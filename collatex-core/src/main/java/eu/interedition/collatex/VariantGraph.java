package eu.interedition.collatex;

import com.google.common.collect.RowSortedTable;

import java.util.Set;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public interface VariantGraph {
  Vertex getStart();

  Vertex getEnd();

  Set<Transposition> transpositions();

  Iterable<Vertex> vertices();

  Iterable<Vertex> vertices(Set<Witness> witnesses);

  Iterable<Edge> edges();

  Iterable<Edge> edges(Set<Witness> witnesses);

  Vertex add(Token token);

  Edge connect(Vertex from, Vertex to, Set<Witness> witnesses);

  Transposition transpose(Vertex from, Vertex to, int transpId);

  boolean isNear(Vertex a, Vertex b);

  boolean verticesAreAdjacent(Vertex a, Vertex b);

  Edge edgeBetween(Vertex a, Vertex b);

  Set<Witness> witnesses();

  VariantGraph join();

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

    VariantGraph getGraph();

    Vertex from();

    Vertex to();

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

    Iterable<Vertex> vertices(Vertex to);

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
