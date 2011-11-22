package eu.interedition.markup.name;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import eu.interedition.markup.NodeWrapper;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import static eu.interedition.markup.MarkupRelationshipType.IS_NAMESPACE_OF;
import static org.neo4j.graphdb.Direction.INCOMING;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class Namespace extends NodeWrapper {

    public Namespace(Node node) {
        super(node);
    }

    public Namespace(Node node, String uri) {
        this(node);
        node.setProperty("uri", uri);
    }

    public String getUri() {
        return (String) node.getProperty("uri");
    }

    public Iterable<Name> getNames() {
        return Iterables.transform(node.getRelationships(IS_NAMESPACE_OF, INCOMING), new Function<Relationship, Name>() {
            @Override
            public Name apply(Relationship input) {
                return new Name(input.getStartNode());
            }
        });
    }
}
