package eu.interedition.web.markup;

import org.neo4j.graphdb.Node;

import static eu.interedition.web.markup.MarkupRelationshipType.IS_NAMESPACE_OF;
import static org.neo4j.graphdb.Direction.OUTGOING;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class Name extends NodeWrapper {

    public Name(Node node) {
        super(node);
    }

    public Name(Node node, String localName) {
        this(node);
        node.setProperty("local_name", localName);
    }

    public Namespace getNamespace() {
        return new Namespace(node.getSingleRelationship(IS_NAMESPACE_OF, OUTGOING).getEndNode());
    }

    public String getLocalName() {
        return (String) node.getProperty("local_name");
    }

    public static String toString(String uri, String localName) {
        return new StringBuilder("{").append(uri).append("}").append(localName).toString();
    }

    @Override
    public String toString() {
        return toString(getNamespace().getUri(), getLocalName());
    }
}
