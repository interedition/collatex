package eu.interedition.text.xml;

import com.google.common.collect.Maps;
import eu.interedition.text.QName;
import eu.interedition.text.mem.SimpleQName;

import javax.xml.stream.XMLStreamReader;
import java.util.Collections;
import java.util.Map;

import static javax.xml.XMLConstants.XMLNS_ATTRIBUTE_NS_URI;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class XMLEntity {
  private final QName name;
  private final Map<QName, String> attributes;


  XMLEntity(QName name) {
    this(name, Maps.<QName, String>newHashMap());
  }

  XMLEntity(QName name, QName attrName, String attrValue) {
    this(name, Collections.singletonMap(attrName, attrValue));
  }

  XMLEntity(QName name, Map<QName, String> attributes) {
    this.name = name;
    this.attributes = attributes;
  }

  public QName getName() {
    return name;
  }

  public Map<QName, String> getAttributes() {
    return attributes;
  }

  public static XMLEntity newComment(XMLStreamReader reader) {
    return new XMLEntity(SimpleQName.COMMENT_QNAME, SimpleQName.COMMENT_TEXT_QNAME, reader.getText());
  }

  public static XMLEntity newPI(XMLStreamReader reader) {
    final Map<QName, String> attributes = Maps.newHashMap();
    attributes.put(SimpleQName.PI_TARGET_QNAME, reader.getPITarget());
    final String data = reader.getPIData();
    if (data != null) {
      attributes.put(SimpleQName.PI_DATA_QNAME, data);
    }
    return new XMLEntity(SimpleQName.PI_QNAME, attributes);
  }

  public static XMLEntity newElement(XMLStreamReader reader) {
    final int attributeCount = reader.getAttributeCount();
    final Map<QName, String> attributes = Maps.newHashMapWithExpectedSize(attributeCount);
    for (int ac = 0; ac < attributeCount; ac++) {
      final javax.xml.namespace.QName attrQName = reader.getAttributeName(ac);
      if (XMLNS_ATTRIBUTE_NS_URI.equals(attrQName.getNamespaceURI())) {
        continue;
      }
      attributes.put(new SimpleQName(attrQName), reader.getAttributeValue(ac));
    }
    return new XMLEntity(new SimpleQName(reader.getName()), attributes);
  }
}
