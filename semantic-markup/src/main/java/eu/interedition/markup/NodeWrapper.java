package eu.interedition.markup;

import org.neo4j.graphdb.Node;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class NodeWrapper {
    protected final Node node;

    protected NodeWrapper(Node node) {
        this.node = node;
    }

    public Node getNode() {
        return node;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && getClass().equals(obj.getClass())) {
            return node.equals(((NodeWrapper) obj).node);
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return node.hashCode();
    }
}
