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
package eu.interedition.text;

import eu.interedition.text.mem.SimpleQName;

import javax.xml.XMLConstants;
import java.net.URI;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public interface TextConstants {
  final URI XML_NS_URI = URI.create(XMLConstants.XML_NS_URI);
  final URI TEI_NS = URI.create("http://www.tei-c.org/ns/1.0");
  final URI INTEREDITION_NS_URI = URI.create("http://interedition.eu/ns");

  final QName XML_ID_ATTR_NAME = new SimpleQName(XML_NS_URI, "id");
  final QName XML_SPACE_ATTR_NAME = new SimpleQName(XML_NS_URI, "space");

  final URI CLIX_NS = URI.create("http://lmnl.net/clix");
  final String CLIX_NS_PREFIX = "c";
  final QName CLIX_START_ATTR_NAME = new SimpleQName(CLIX_NS, "sID");
  final QName CLIX_END_ATTR_NAME = new SimpleQName(CLIX_NS, "eID");
}
