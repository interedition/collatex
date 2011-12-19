package eu.interedition.web.markup;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.TermQuery;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.index.Index;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@Service
public class NameManager {

    @Autowired
    private GraphDatabaseService db;

    public Namespace create(String uri) {
        final Node node = db.createNode();
        final Namespace ns = new Namespace(node, uri);
        node.createRelationshipTo(db.getReferenceNode(), MarkupRelationshipType.IS_NAMESPACE_OF);

        final Index<Node> index = namespaceIndex();
        index.add(node, "uri", uri);
        return ns;
    }

    public Name create(Namespace namespace, String localName) {
        final Node node = db.createNode();
        node.createRelationshipTo(namespace.getNode(), MarkupRelationshipType.IS_NAMESPACE_OF);

        final Index<Node> index = nameIndex();
        index.add(node, "name", Name.toString(namespace.getUri(), localName));

        return new Name(node, localName);
    }

    public Namespace find(String uri) {
        final Node node = namespaceIndex().query(new TermQuery(new Term("uri", uri))).getSingle();
        return (node == null ? null : new Namespace(node));
    }

    public Name find(Namespace namespace, String localName) {
        final Node node = nameIndex().query(new TermQuery(new Term("name", Name.toString(namespace.getUri(), localName)))).getSingle();
        return (node == null ? null : new Name(node));
    }

    public Namespace get(String uri) {
        final Namespace namespace = find(uri);
        return (namespace == null ? create(uri) : namespace);
    }

    public Name get(Namespace namespace, String localName) {
        final Name name = find(namespace, localName);
        return (name == null ? create(namespace, localName) : name);
    }

    public Index<Node> nameIndex() {
        return db.index().forNodes("name");
    }

    public Index<Node> namespaceIndex() {
        return db.index().forNodes("namespace");
    }
}
