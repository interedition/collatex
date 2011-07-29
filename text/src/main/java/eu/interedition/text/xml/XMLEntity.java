package eu.interedition.text.xml;

import com.google.common.base.Objects;
import com.google.common.collect.Maps;
import eu.interedition.text.QName;
import eu.interedition.text.mem.SimpleQName;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLStreamReader;
import java.util.Collections;
import java.util.Map;

import static javax.xml.XMLConstants.XMLNS_ATTRIBUTE_NS_URI;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class XMLEntity {
  private final String prefix;
  private final QName name;
  private final Map<QName, String> attributes;


  XMLEntity(QName name, String prefix) {
    this(name, prefix, Maps.<QName, String>newHashMap());
  }

  XMLEntity(QName name, String prefix, QName attrName, String attrValue) {
    this(name, prefix, Collections.singletonMap(attrName, attrValue));
  }

  XMLEntity(QName name, String prefix, Map<QName, String> attributes) {
    this.name = name;
    this.prefix = prefix;
    this.attributes = attributes;
  }

  public String getPrefix() {
    return prefix;
  }

  public QName getName() {
    return name;
  }

  public Map<QName, String> getAttributes() {
    return attributes;
  }

  public static XMLEntity newComment(XMLStreamReader reader) {
    return new XMLEntity(SimpleQName.COMMENT_QNAME, XMLConstants.DEFAULT_NS_PREFIX);
  }

  public static XMLEntity newPI(XMLStreamReader reader) {
    final Map<QName, String> attributes = Maps.newHashMap();
    attributes.put(SimpleQName.PI_TARGET_QNAME, reader.getPITarget());
    final String data = reader.getPIData();
    if (data != null) {
      attributes.put(SimpleQName.PI_DATA_QNAME, data);
    }
    return new XMLEntity(SimpleQName.PI_QNAME, XMLConstants.DEFAULT_NS_PREFIX, attributes);
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
    return new XMLEntity(new SimpleQName(reader.getName()), XMLConstants.DEFAULT_NS_PREFIX, attributes);
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this).addValue(name).toString();
  }
}
