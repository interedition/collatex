package eu.interedition.collatex.graph;

import org.neo4j.graphdb.RelationshipType;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public enum GraphRelationshipType implements RelationshipType {
  VARIANT_GRAPH, EDIT_GRAPH, START_END, PATH, TRANSPOSITION;
}
