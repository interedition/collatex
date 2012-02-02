package eu.interedition.text.xml;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import eu.interedition.text.mem.SimpleAnnotation;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;

import java.util.ArrayDeque;
import java.util.Iterator;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class XMLNodePath extends ArrayDeque<Integer> implements Comparable<XMLNodePath> {

  public XMLNodePath() {
    super(10);
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
    for (Integer nodePos : this) {
      nodePathArray.add(nodePos);
    }
    return nodePathArray;
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
    final Iterator<Integer> it = iterator();
    final Iterator<Integer> otherIt = o.iterator();
    while (it.hasNext() && otherIt.hasNext()) {
      final Integer pos = it.next();
      final Integer otherPos = otherIt.next();
      if (pos != otherPos) {
        return pos - otherPos;
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
