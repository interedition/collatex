package eu.interedition.text.graph;

import eu.interedition.text.Text;
import org.neo4j.graphdb.Node;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class GraphText implements Text {
  static final String PROP_TYPE = "text_type";
  static final String PROP_LENGTH = "text_length";
  static final String PROP_DIGEST = "text_digest";
  static final String PROP_UUID = "text_uuid";

  final Node node;

  public GraphText(Node node) {
    this.node = node;
  }

  public Node getNode() {
    return node;
  }

  @Override
  public Type getType() {
    return Type.values()[(Integer) node.getProperty(PROP_TYPE)];
  }

  @Override
  public long getLength() {
    return (Long) node.getProperty(PROP_LENGTH);
  }

  @Override
  public String getDigest() {
    return (String) node.getProperty(PROP_DIGEST);
  }

  public String getUUID() {
    return (String) node.getProperty(PROP_UUID);
  }
}
