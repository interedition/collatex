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
import eu.interedition.text.Annotation;
import eu.interedition.text.Name;
import eu.interedition.text.Range;
import eu.interedition.text.Text;
import eu.interedition.text.mem.SimpleAnnotation;
import eu.interedition.text.mem.SimpleName;
import eu.interedition.text.util.Names;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLStreamReader;

import java.net.URI;
import java.util.Iterator;
import java.util.Map;

import static eu.interedition.text.mem.SimpleAnnotation.JSON;
import static javax.xml.XMLConstants.XMLNS_ATTRIBUTE_NS_URI;
import static javax.xml.XMLConstants.XML_NS_URI;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class XMLEntity {
  public static final Name COMMENT_QNAME = new SimpleName(XML_NS_URI, "comment");

  public static final Name PI_QNAME = new SimpleName(XML_NS_URI, "pi");

  public static final String PI_TARGET_ATTR = "xml:piTarget";
  public static final String PI_DATA_ATTR = "xml:piData";


  private final String prefix;
  private final Name name;
  private final ObjectNode attributes;


  XMLEntity(Name name, String prefix) {
    this(name, prefix, JSON.createObjectNode());
  }

  XMLEntity(Name name, String prefix, ObjectNode attributes) {
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

  public ObjectNode getAttributes() {
    return attributes;
  }

  public static XMLEntity newComment(XMLStreamReader reader) {
    return new XMLEntity(COMMENT_QNAME, XMLConstants.DEFAULT_NS_PREFIX);
  }

  public static XMLEntity newPI(XMLStreamReader reader) {
    final ObjectNode attributes = JSON.createObjectNode();
    attributes.put(PI_TARGET_ATTR, reader.getPITarget());

    final String data = reader.getPIData();
    if (data != null) {
      attributes.put(PI_DATA_ATTR, data);
    }
    return new XMLEntity(PI_QNAME, XMLConstants.DEFAULT_NS_PREFIX, attributes);
  }

  public static XMLEntity newElement(XMLStreamReader reader) {
    return new XMLEntity(new SimpleName(reader.getName()), XMLConstants.DEFAULT_NS_PREFIX, attributesToData(reader));
  }

  public static ObjectNode attributesToData(XMLStreamReader reader) {
    final int attributeCount = reader.getAttributeCount();
    final Map<Name, String> attributes = Maps.newHashMapWithExpectedSize(attributeCount);
    for (int ac = 0; ac < attributeCount; ac++) {
      attributes.put(new SimpleName(reader.getAttributeName(ac)), reader.getAttributeValue(ac));
    }
    return attributesToData(attributes);
  }

  public static ObjectNode attributesToData(Map<Name, String> attributes) {
    final ObjectNode data = JSON.createObjectNode();
    for (Map.Entry<Name, String> attribute : attributes.entrySet()) {
      final URI namespace = attribute.getKey().getNamespace();
      if (namespace != null && XMLNS_ATTRIBUTE_NS_URI.equals(namespace.toString())) {
        continue;
      }
      data.put(attribute.getKey().toString(), attribute.getValue());
    }
    return data;
  }

  public static Map<Name, String> dataToAttributes(JsonNode data) {
    final Map<Name, String> attributes = Maps.newHashMapWithExpectedSize(data.size());
    for (Iterator<String> attrNameIt = data.getFieldNames(); attrNameIt.hasNext(); ) {
      try {
        final String attrName = attrNameIt.next();
        final JsonNode value = data.get(attrName);
        attributes.put(Names.fromString(attrName), value.isTextual() ? value.getTextValue() : value.toString());
      } catch (IllegalArgumentException e) {
      }
    }
    return attributes;
  }

  public Annotation toAnnotation(Text text, long offset) {
    return new SimpleAnnotation(text, name, new Range(offset, offset), attributes);
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this).addValue(name).toString();
  }
}
