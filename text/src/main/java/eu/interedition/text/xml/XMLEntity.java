/*
 * #%L
 * Text: A text model with range-based markup via standoff annotations.
 * %%
 * Copyright (C) 2010 - 2011 The Interedition Development Group
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package eu.interedition.text.xml;

import com.google.common.base.Objects;
import com.google.common.collect.Maps;
import eu.interedition.text.Name;
import eu.interedition.text.mem.SimpleName;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLStreamReader;
import java.util.Collections;
import java.util.Map;

import static javax.xml.XMLConstants.XMLNS_ATTRIBUTE_NS_URI;
import static javax.xml.XMLConstants.XML_NS_URI;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class XMLEntity {
  public static final Name COMMENT_QNAME = new SimpleName(XML_NS_URI, "comment");

  public static final Name PI_QNAME = new SimpleName(XML_NS_URI, "pi");
  public static final Name PI_TARGET_QNAME = new SimpleName(XML_NS_URI, "piTarget");
  public static final Name PI_DATA_QNAME = new SimpleName(XML_NS_URI, "piDarget");


  private final String prefix;
  private final Name name;
  private final Map<Name, String> attributes;


  XMLEntity(Name name, String prefix) {
    this(name, prefix, Maps.<Name, String>newHashMap());
  }

  XMLEntity(Name name, String prefix, Name attrName, String attrValue) {
    this(name, prefix, Collections.singletonMap(attrName, attrValue));
  }

  XMLEntity(Name name, String prefix, Map<Name, String> attributes) {
    this.name = name;
    this.prefix = prefix;
    this.attributes = attributes;
  }

  public String getPrefix() {
    return prefix;
  }

  public Name getName() {
    return name;
  }

  public Map<Name, String> getAttributes() {
    return attributes;
  }

  public static XMLEntity newComment(XMLStreamReader reader) {
    return new XMLEntity(COMMENT_QNAME, XMLConstants.DEFAULT_NS_PREFIX);
  }

  public static XMLEntity newPI(XMLStreamReader reader) {
    final Map<Name, String> attributes = Maps.newHashMap();
    attributes.put(PI_TARGET_QNAME, reader.getPITarget());
    final String data = reader.getPIData();
    if (data != null) {
      attributes.put(PI_DATA_QNAME, data);
    }
    return new XMLEntity(PI_QNAME, XMLConstants.DEFAULT_NS_PREFIX, attributes);
  }

  public static XMLEntity newElement(XMLStreamReader reader) {
    final int attributeCount = reader.getAttributeCount();
    final Map<Name, String> attributes = Maps.newHashMapWithExpectedSize(attributeCount);
    for (int ac = 0; ac < attributeCount; ac++) {
      final javax.xml.namespace.QName attrQName = reader.getAttributeName(ac);
      if (XMLNS_ATTRIBUTE_NS_URI.equals(attrQName.getNamespaceURI())) {
        continue;
      }
      attributes.put(new SimpleName(attrQName), reader.getAttributeValue(ac));
    }
    return new XMLEntity(new SimpleName(reader.getName()), XMLConstants.DEFAULT_NS_PREFIX, attributes);
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this).addValue(name).toString();
  }
}
