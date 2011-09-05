package eu.interedition.text.graph;

import eu.interedition.text.QName;
import eu.interedition.text.util.QNames;
import org.neo4j.graphdb.Node;

import java.net.URI;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class GraphQName implements QName {
  static final String PROP_NS = "name_ns";
  static final String PROP_LOCAL_NAME = "name_ln";

  private final Node node;

  public GraphQName(Node node) {
    this.node = node;
  }

  public Node getNode() {
    return node;
  }

  @Override
  public URI getNamespaceURI() {
    final String nsUri = (String) node.getProperty(PROP_NS);
    return (nsUri == null ? null : URI.create(nsUri));
  }

  @Override
  public String getLocalName() {
    return (String) node.getProperty(PROP_LOCAL_NAME);
  }

  @Override
  public int compareTo(QName o) {
    return QNames.COMPARATOR.compare(this ,o);
  }

  @Override
  public int hashCode() {
    return QNames.hashCode(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj != null && obj instanceof QName) {
      return QNames.equal(this, (QName) obj);
    }
    return super.equals(obj);
  }
}
