package eu.interedition.text.xml;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import eu.interedition.text.Annotation;
import eu.interedition.text.TextConstants;
import eu.interedition.text.mem.SimpleAnnotation;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;

import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.Iterator;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class XMLNodePath extends ArrayDeque<Integer> implements Comparable<XMLNodePath> {
  private static final String XML_NODE_ATTR = TextConstants.XML_NODE_ATTR_NAME.toString();

  public static Comparator<Annotation> ANNOTATION_COMPARATOR = new Comparator<Annotation>() {
    @Override
    public int compare(Annotation o1, Annotation o2) {
      final XMLNodePath np1 = get(o1);
      if (np1 == null) {
        return 0;
      }

      final XMLNodePath np2 = get(o2);
      return (np2 == null ? 0 : np1.compareTo(np2));
    }
  };

  public XMLNodePath() {
    super(10);
  }

  public XMLNodePath(XMLNodePath nodePath) {
    super(nodePath);
  }

  public XMLNodePath(JsonNode nodePath) {
    super(nodePath.size());
    Preconditions.checkArgument(nodePath.isArray());
    for (JsonNode pos : nodePath) {
      Preconditions.checkArgument(pos.isInt());
      push(pos.getIntValue());
    }
  }

  public ArrayNode toArrayNode() {
    final ArrayNode nodePathArray = SimpleAnnotation.JSON.createArrayNode();
    final Iterator<Integer> it = descendingIterator();
    while (it.hasNext()) {
      nodePathArray.add(it.next());
    }
    return nodePathArray;
  }

  public static XMLNodePath get(Annotation annotation) {
    final JsonNode nodePath = annotation.getData().get(XML_NODE_ATTR);
    return nodePath == null ? null : new XMLNodePath(nodePath);
  }

  public void set(ObjectNode data) {
    data.put(XML_NODE_ATTR, toArrayNode());
  }

  @Override
  public boolean equals(Object obj) {
    if (obj != null && obj instanceof XMLNodePath) {
      return compareTo((XMLNodePath) obj) == 0;
    }
    return super.equals(obj);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(toArray(new Object[size()]));
  }

  @Override
  public int compareTo(XMLNodePath o) {
    final Iterator<Integer> it = descendingIterator();
    final Iterator<Integer> otherIt = o.descendingIterator();

    int result;
    while (it.hasNext() && otherIt.hasNext()) {
      result = it.next().compareTo(otherIt.next());
      if (result != 0) {
        return result;
      }
    }

    if (it.hasNext()) {
      return 1;
    } else if (otherIt.hasNext()) {
      return -1;
    }
    return 0;
  }


}
