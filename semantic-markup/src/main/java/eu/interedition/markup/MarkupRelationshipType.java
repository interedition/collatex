package eu.interedition.markup;

import org.neo4j.graphdb.RelationshipType;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public enum MarkupRelationshipType implements RelationshipType {
    IS_NAMESPACE_OF, NAMES_ANNOTATION_TYPE, NAMES_PROPERTY_TYPE, DEFINED_IN, IS_CONTAINED_IN, HAS_PROPERTY, IS_PROPERTY_OF, CONTAINS
}
