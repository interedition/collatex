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

import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import eu.interedition.text.*;
import eu.interedition.text.event.AnnotationEventSource;
import eu.interedition.text.event.ExceptionPropagatingAnnotationEventAdapter;
import eu.interedition.text.mem.SimpleQName;
import org.springframework.beans.factory.annotation.Required;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.net.URI;
import java.util.*;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class XMLSerializer {
  private AnnotationEventSource eventSource;

  @Required
  public void setEventSource(AnnotationEventSource eventSource) {
    this.eventSource = eventSource;
  }

  public void serialize(final ContentHandler xml, Text text, final XMLSerializerConfiguration config) throws XMLStreamException, IOException {
    try {
      eventSource.listen(new SerializingListener(xml, config), text, config.getQuery(), null);
    } catch (Throwable t) {
      Throwables.propagateIfInstanceOf(t, IOException.class);
      Throwables.propagateIfInstanceOf(Throwables.getRootCause(t), XMLStreamException.class);
      throw Throwables.propagate(t);
    }
  }

  private class SerializingListener extends ExceptionPropagatingAnnotationEventAdapter {
    private final ContentHandler xml;
    private final XMLSerializerConfiguration config;
    private final List<QName> hierarchy;

    private final Map<URI, String> namespaceMappings = Maps.newHashMap();
    private final Stack<Set<URI>> namespaceMappingStack = new Stack<Set<URI>>();
    private final Map<String, Integer> clixIdIncrements = Maps.newHashMap();
    private final Map<Annotation, String> clixIds = Maps.newHashMap();

    private boolean rootWritten = false;
    private Ordering<Annotation> annotationOrdering;

    private SerializingListener(ContentHandler xml, XMLSerializerConfiguration config) {
      this.xml = xml;
      this.config = config;
      this.hierarchy = (config.getHierarchy() == null ? Collections.<QName>emptyList() : config.getHierarchy());
      this.annotationOrdering = Ordering.from(new HierarchyAwareAnnotationComparator(this.hierarchy));
      this.namespaceMappings.put(URI.create(XMLConstants.XML_NS_URI), XMLConstants.XML_NS_PREFIX);
      this.namespaceMappings.put(URI.create(XMLConstants.XMLNS_ATTRIBUTE_NS_URI), XMLConstants.XMLNS_ATTRIBUTE);
    }

    @Override
    protected void doStart() throws Exception {
      xml.startDocument();

      final QName rootName = config.getRootName();
      if (rootName != null) {
        startElement(rootName, Collections.<QName, String>emptyMap());
      }
    }

    @Override
    protected void doStart(long offset, Map<Annotation, Map<QName, String>> annotations) throws Exception {
      for (Annotation a : annotationOrdering.immutableSortedCopy(annotations.keySet())) {
        final QName name = a.getName();
        Map<QName, String> attributes = annotations.get(a);
        if (!rootWritten || hierarchy.contains(name)) {
          startElement(name, attributes);
        } else {
          final String localName = name.getLocalName();

          Integer id = clixIdIncrements.get(localName);
          id = (id == null ? 0 : id + 1);
          final String clixId = "clix:" + localName + "-" + id;

          attributes = Maps.newHashMap(attributes);
          attributes.put(TextConstants.CLIX_START_ATTR_NAME, clixId);
          emptyElement(name, attributes);

          clixIdIncrements.put(localName, id);
          clixIds.put(a, clixId);
        }
      }
    }

    @Override
    protected void doEmpty(long offset, Map<Annotation, Map<QName, String>> annotations) throws Exception {
      for (Annotation a : annotationOrdering.immutableSortedCopy(annotations.keySet())) {
        emptyElement(a.getName(), annotations.get(a));
      }
    }

    @Override
    protected void doEnd(long offset, Map<Annotation, Map<QName, String>> annotations) throws Exception {
      for (Annotation a : annotationOrdering.reverse().immutableSortedCopy(annotations.keySet())) {
        final String clixId = clixIds.get(a);
        if (clixId == null) {
          endElement(a.getName());
        } else {
          final Map<QName, String> attributes = Maps.newHashMap();
          attributes.put(TextConstants.CLIX_END_ATTR_NAME, clixId);
          emptyElement(a.getName(), attributes);

          clixIds.remove(a);
        }

      }
    }

    @Override
    protected void doText(Range r, String text) throws Exception {
      final char[] chars = text.toCharArray();
      xml.characters(chars, 0, chars.length);
    }

    @Override
    protected void doEnd() throws Exception {
      final QName rootName = config.getRootName();
      if (rootName != null) {
        endElement(rootName);
      }
      xml.endDocument();
    }

    private void emptyElement(QName name, Map<QName, String> attributes) throws SAXException {
      startElement(name, attributes);
      endElement(name);
    }

    private void startElement(QName name, Map<QName, String> attributes) throws SAXException {
      namespaceMappingStack.push(new HashSet<URI>());

      final Map<QName, String> nsAttributes = Maps.newHashMap();
      if (!rootWritten) {
        for (Map.Entry<String, URI> mapping : config.getNamespaceMappings().entrySet()) {
          mapNamespace(mapping.getValue(), mapping.getKey(), nsAttributes);
        }
        mapNamespace(TextConstants.CLIX_NS, TextConstants.CLIX_NS_PREFIX, nsAttributes);
        rootWritten = true;
      }
      for (QName n : Iterables.concat(attributes.keySet(), Collections.singleton(name))) {
        final URI ns = n.getNamespaceURI();
        if (ns == null || namespaceMappings.containsKey(ns)) {
          continue;
        }
        int count = 0;
        String newPrefix = "ns" + count;
        while (true) {
          if (!namespaceMappings.containsKey(newPrefix)) {
            break;
          }
          newPrefix = "ns" + (++count);
        }
        mapNamespace(ns, newPrefix, nsAttributes);
      }

      final Map<QName, String> mergedAttributes = Maps.newLinkedHashMap();
      mergedAttributes.putAll(nsAttributes);
      mergedAttributes.putAll(attributes);
      xml.startElement(toNamespace(name.getNamespaceURI()), name.getLocalName(), toQNameStr(name), toAttributes(mergedAttributes));
    }

    private void mapNamespace(URI namespace, String prefix, Map<QName, String> nsAttributes) throws SAXException {
      final String uri = namespace.toString();
      namespaceMappings.put(namespace, prefix);
      namespaceMappingStack.peek().add(namespace);
      if (prefix.length() == 0) {
        nsAttributes.put(new SimpleQName((URI) null, XMLConstants.XMLNS_ATTRIBUTE), uri);
      } else {
        nsAttributes.put(new SimpleQName(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, prefix), uri);
        xml.startPrefixMapping(prefix, uri);
      }
    }

    private void endElement(QName name) throws SAXException {
      xml.endElement(toNamespace(name.getNamespaceURI()), name.getLocalName(), toQNameStr(name));

      for (URI namespace : namespaceMappingStack.pop()) {
        xml.endPrefixMapping(namespaceMappings.remove(namespace));
      }
    }

    private String toNamespace(URI uri) {
      return (uri == null ? "" : uri.toString());
    }

    private String toQNameStr(QName name) {
      final URI ns = name.getNamespaceURI();
      final String localName = name.getLocalName();

      if (ns == null) {
        return localName;
      } else {
        final String prefix = namespaceMappings.get(ns);
        return (prefix.length() == 0 ? localName : prefix + ":" + localName);
      }
    }

    private QName toQName(String str) {
      final int colon = str.indexOf(':');
      return (colon >= 0 ? toQName(str.substring(0, colon), str.substring(colon + 1)) : toQName(null, str));
    }

    private QName toQName(String uri, String localName) {
      return new SimpleQName(URI.create(uri), localName);
    }

    private Attributes toAttributes(final Map<QName, String> attributes) {
      return new Attributes() {
        final List<QName> names = Lists.newArrayList(attributes.keySet());

        public int getLength() {
          return names.size();
        }

        public String getURI(int index) {
          return toNamespace(names.get(index).getNamespaceURI());
        }

        public String getLocalName(int index) {
          return names.get(index).getLocalName();
        }

        public String getQName(int index) {
          return toQNameStr(names.get(index));
        }

        public String getType(int index) {
          return (index >= 0 && index < names.size() ? "CDATA" : null);
        }

        public String getValue(int index) {
          return attributes.get(names.get(index));
        }

        public int getIndex(String uri, String localName) {
          return names.indexOf(toQName(uri, localName));
        }

        public int getIndex(String qName) {
          return names.indexOf(toQName(qName));
        }

        public String getType(String uri, String localName) {
          return names.indexOf(toQName(uri, localName)) >= 0 ? "CDATA" : null;
        }

        public String getType(String qName) {
          return names.indexOf(toQName(qName)) >= 0 ? "CDATA" : null;
        }

        public String getValue(String uri, String localName) {
          return attributes.get(toQName(uri, localName));
        }

        public String getValue(String qName) {
          return attributes.get(toQName(qName));
        }
      };
    }
  }

  private static class HierarchyAwareAnnotationComparator implements Comparator<Annotation> {
    private Ordering<QName> hierarchyOrdering;
    private final List<QName> hierarchy;

    private HierarchyAwareAnnotationComparator(List<QName> hierarchy) {
      this.hierarchy = hierarchy;
      this.hierarchyOrdering = Ordering.explicit(hierarchy);
    }

    public int compare(Annotation o1, Annotation o2) {
      int result = o1.getRange().compareTo(o2.getRange());
      if (result != 0) {
        return result;
      }

      final QName o1Name = o1.getName();
      final QName o2Name = o2.getName();
      if (hierarchy.contains(o1Name) && hierarchy.contains(o2Name)) {
        result = hierarchyOrdering.compare(o1Name, o2Name);
      }
      if (result != 0) {
        return result;
      }

      return o1.compareTo(o2);
    }
  }
}
