package eu.interedition.text.graph;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import eu.interedition.text.QName;
import eu.interedition.text.QNameRepository;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.TermQuery;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.index.Index;

import java.net.URI;
import java.util.Collections;
import java.util.Set;

import static eu.interedition.text.graph.TextRelationshipType.HAS_NAME;
import static org.apache.lucene.search.BooleanClause.Occur.MUST;
import static org.apache.lucene.search.BooleanClause.Occur.SHOULD;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class GraphQNameRepository implements QNameRepository {
  public static final String INDEX_QNAME = "text_qname";

  private GraphDatabaseService db;

  public void setGraphDataSource(GraphDataSource ds) {
    this.db = ds.getGraphDatabaseService();
  }

  @Override
  public QName get(QName name) {
    return Iterables.getOnlyElement(get(Collections.singleton(name)));
  }

  @Override
  public Set<QName> get(Set<QName> names) {
    names = Sets.newHashSet(names);

    final Index<Node> nameIndex = db.index().forNodes(INDEX_QNAME);
    final Set<QName> result = Sets.newHashSet();

    final BooleanQuery query = new BooleanQuery();
    for (QName name : names) {
      final BooleanQuery nameQuery = new BooleanQuery();
      final URI ns = name.getNamespaceURI();
      nameQuery.add(new TermQuery(new Term(GraphQName.PROP_NS, ns == null ? "" : ns.toString())), MUST);
      nameQuery.add(new TermQuery(new Term(GraphQName.PROP_LOCAL_NAME, name.getLocalName())), MUST);
      query.add(nameQuery, SHOULD);
    }
    for (Node nameNode : nameIndex.query(query)) {
      result.add(new GraphQName(nameNode));
    }
    names.removeAll(result);

    for (QName name : names) {
      final Node nameNode = db.createNode();
      final URI ns = name.getNamespaceURI();
      final String localName = name.getLocalName();

      nameNode.setProperty(GraphQName.PROP_NS, ns == null ? null : ns.toString());
      nameNode.setProperty(GraphQName.PROP_LOCAL_NAME, localName);
      db.getReferenceNode().createRelationshipTo(nameNode, HAS_NAME);

      nameIndex.add(nameNode, GraphQName.PROP_NS, ns == null ? "" : ns.toString());
      nameIndex.add(nameNode, GraphQName.PROP_LOCAL_NAME, localName);

      result.add(new GraphQName(nameNode));
    }

    return result;
  }
}
