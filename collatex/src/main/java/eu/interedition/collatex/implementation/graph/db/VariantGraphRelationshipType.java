package eu.interedition.collatex.implementation.graph.db;

import org.neo4j.graphdb.RelationshipType;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public enum VariantGraphRelationshipType implements RelationshipType {
  VARIANT_GRAPH, START_END, PATH;
}
