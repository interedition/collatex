package eu.interedition.text.graph;

import org.neo4j.graphdb.RelationshipType;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public enum TextRelationshipType implements RelationshipType {
  ANNOTATES, NAMES, HAS_NAME, HAS_TEXT
}
