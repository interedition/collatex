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
package eu.interedition.text.mem;

import com.google.common.base.Strings;
import eu.interedition.text.Name;
import eu.interedition.text.util.Names;

import java.net.URI;

public class SimpleName implements Name {
  protected final URI namespace;
  protected final String localName;

  public SimpleName(URI namespace, String localName) {
    this.namespace = namespace;
    this.localName = localName;
  }

  public SimpleName(String namespace, String localName) {
    this(namespace == null ? null : URI.create(namespace), localName);
  }

  public SimpleName(javax.xml.namespace.QName name) {
    final String ns = name.getNamespaceURI();
    this.namespace = Strings.isNullOrEmpty(ns) ? null : URI.create(ns);
    this.localName = name.getLocalPart();
  }

  public URI getNamespace() {
    return namespace;
  }

  public String getLocalName() {
    return localName;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj != null && obj instanceof Name) {
      return Names.equal(this, (Name) obj);
    }
    return super.equals(obj);
  }

  @Override
  public int hashCode() {
    return Names.hashCode(this);
  }

  public int compareTo(Name o) {
    return Names.COMPARATOR.compare(this, o);
  }

  @Override
  public String toString() {
    return Names.toString(this);
  }
}
