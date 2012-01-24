package eu.interedition.web.markup;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@Service
public class TypeManager {

    @Autowired
    private GraphDatabaseService db;

    public AnnotationType createType(Name name, String documentation) {
        final Node node = db.createNode();
        name.getNode().createRelationshipTo(node, MarkupRelationshipType.NAMES_ANNOTATION_TYPE);
        return new AnnotationType(node, documentation);
    }

    public PropertyType createPropertyType(Name name, String documentation) {
        final Node node = db.createNode();
        name.getNode().createRelationshipTo(node, MarkupRelationshipType.NAMES_PROPERTY_TYPE);
        return new PropertyType(node, documentation);
    }

    public Node createContainmentDefinition(Schema in, AnnotationType type, AnnotationType container) {
        final Node containment = db.createNode();
        containment.createRelationshipTo(in.getNode(), MarkupRelationshipType.DEFINED_IN);
        container.getNode().createRelationshipTo(containment, MarkupRelationshipType.CONTAINS);
        type.getNode().createRelationshipTo(containment, MarkupRelationshipType.IS_CONTAINED_IN);
        return containment;
    }

    public void createPropertyDefinition(Schema in, AnnotationType type, PropertyType propertyType) {
        final Node slot = db.createNode();
        slot.createRelationshipTo(in.getNode(), MarkupRelationshipType.DEFINED_IN);
        type.getNode().createRelationshipTo(slot, MarkupRelationshipType.HAS_PROPERTY);
        propertyType.getNode().createRelationshipTo(slot, MarkupRelationshipType.IS_PROPERTY_OF);
    }
}
