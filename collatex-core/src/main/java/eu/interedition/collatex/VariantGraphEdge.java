package eu.interedition.collatex;

import eu.interedition.collatex.neo4j.Neo4jVariantGraph;
import eu.interedition.collatex.neo4j.Neo4jVariantGraphVertex;

import java.util.Set;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public interface VariantGraphEdge {
  boolean traversableWith(Set<Witness> witnesses);

  VariantGraphEdge add(Set<Witness> witnesses);

  Set<Witness> witnesses();

  Neo4jVariantGraph getGraph();

  Neo4jVariantGraphVertex from();

  Neo4jVariantGraphVertex to();

  void delete();
}
