package eu.interedition.markup.type;

import eu.interedition.markup.NodeWrapper;
import org.neo4j.graphdb.Node;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public abstract class DocumentedType extends NodeWrapper {

    protected DocumentedType(Node node) {
        super(node);
    }

    protected DocumentedType(Node node, String documentation) {
        super(node);
        setDocumentation(documentation);
    }

    public String getDocumentation() {
        return (String) node.getProperty("documentation");
    }

    public void setDocumentation(String documentation) {
        if (documentation == null) {
            node.removeProperty("documentation");
        } else {
            node.setProperty("documentation", documentation);
        }
    }
}
