package eu.interedition.web.markup;

import org.neo4j.graphdb.Node;

import static eu.interedition.web.markup.MarkupRelationshipType.NAMES_PROPERTY_TYPE;
import static org.neo4j.graphdb.Direction.INCOMING;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class PropertyType extends DocumentedType {

    public PropertyType(Node node) {
        super(node);
    }

    protected PropertyType(Node node, String documentation) {
        super(node, documentation);
    }

    public Name getName() {
        return new Name(node.getSingleRelationship(NAMES_PROPERTY_TYPE, INCOMING).getStartNode());
    }
}
