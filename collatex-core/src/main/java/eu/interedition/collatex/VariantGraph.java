package eu.interedition.collatex;

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

  Transposition transpose(Set<Vertex> vertices);

  Edge edgeBetween(Vertex a, Vertex b);

  Set<Witness> witnesses();

  /**
   * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
   */
  interface Edge {

    VariantGraph graph();

    Edge add(Set<Witness> witnesses);

    Set<Witness> witnesses();

    Vertex from();

    Vertex to();

    void delete();
  }

  /**
   * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
   */
  interface Vertex {
    Iterable<? extends Edge> incoming();

    Iterable<? extends Edge> incoming(Set<Witness> witnesses);

    Iterable<? extends Edge> outgoing();

    Iterable<? extends Edge> outgoing(Set<Witness> witnesses);

    Iterable<? extends Transposition> transpositions();

    Set<Token> tokens();

    Set<Token> tokens(Set<Witness> witnesses);

    Set<Witness> witnesses();

    void add(Iterable<Token> tokens);

    VariantGraph graph();

    void delete();
  }

  /**
   * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
   */
  interface Transposition extends Iterable<Vertex> {
    void delete();
  }
}
