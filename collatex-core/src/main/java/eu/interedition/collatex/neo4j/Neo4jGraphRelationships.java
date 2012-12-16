package eu.interedition.collatex.neo4j;

import org.neo4j.graphdb.RelationshipType;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public enum Neo4jGraphRelationships implements RelationshipType {
  VARIANT_GRAPH, START_END, PATH, TRANSPOSITION;
}
