package eu.interedition.text.graph;

import eu.interedition.text.Annotation;
import eu.interedition.text.QName;
import eu.interedition.text.Range;
import eu.interedition.text.Text;
import eu.interedition.text.util.Annotations;
import org.neo4j.graphdb.Node;

import static eu.interedition.text.graph.TextRelationshipType.ANNOTATES;
import static eu.interedition.text.graph.TextRelationshipType.NAMES;
import static org.neo4j.graphdb.Direction.INCOMING;
import static org.neo4j.graphdb.Direction.OUTGOING;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class GraphAnnotation implements Annotation {
  public static final String PROP_ID = "ann_id";
  public static final String PROP_TEXT = "ann_text";
  public static final String PROP_RANGE_START = "ann_range_start";
  public static final String PROP_RANGE_END = "ann_range_end";
  public static final String PROP_RANGE_LENGTH = "ann_length";

  private final Node node;

  public GraphAnnotation(Node node) {
    this.node = node;
  }

  public Node getNode() {
    return node;
  }

  @Override
  public Text getText() {
    return new GraphText(node.getSingleRelationship(ANNOTATES, OUTGOING).getEndNode());
  }

  @Override
  public QName getName() {
    return new GraphQName(node.getSingleRelationship(NAMES, INCOMING).getStartNode());
  }

  @Override
  public Range getRange() {
    return new Range((Long) node.getProperty(PROP_RANGE_START), (Long) node.getProperty(PROP_RANGE_END));
  }

  @Override
  public int compareTo(Annotation o) {
    return Annotations.compare(this, o).compare(node.getId(), ((GraphAnnotation)o).node.getId()).result();
  }
}
