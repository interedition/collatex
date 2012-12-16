package eu.interedition.collatex;

import eu.interedition.collatex.neo4j.Neo4jVariantGraphVertex;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public interface VariantGraphTransposition {
  Neo4jVariantGraphVertex from();

  Neo4jVariantGraphVertex to();

  Neo4jVariantGraphVertex other(Neo4jVariantGraphVertex vertex);

  void delete();

  int getId();
}
