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
package eu.interedition.text.rdbms;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import eu.interedition.text.Name;
import eu.interedition.text.util.Names;

import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A qualified/ "namespaced" identifier.
 *
 * @author <a href="http://gregor.middell.net/" title="Homepage of Gregor Middell">Gregor Middell</a>
 */
public class RelationalName implements Name {
  private static final Pattern STR_REPR = Pattern.compile("^\\{([^\\}]*)\\}(.+)$");

  private long id;
  private URI namespace;
  private String localName;

  public RelationalName() {
  }

  public RelationalName(long id, URI namespace, String localName) {
    this.id = id;
    this.namespace = namespace;
    this.localName = localName;
  }

  public RelationalName(long id, Name other) {
    this(id, other.getNamespace(), other.getLocalName());
  }

  public RelationalName(URI namespace, String localName) {
    this(0, namespace, localName);
  }

  public RelationalName(String uri, String localName, String qName) {
    this.id = 0;
    if (uri.length() == 0 && localName.length() == 0) {
      this.namespace = null;
      this.localName = qName;
    } else {
      this.namespace = (Strings.isNullOrEmpty(uri) ? null : URI.create(uri));
      this.localName = localName;
    }
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public URI getNamespace() {
    return namespace;
  }

  public void setNamespaceURI(URI namespace) {
    this.namespace = namespace;
  }

  public String getLocalName() {
    return localName;
  }

  public void setLocalName(String localName) {
    this.localName = localName;
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

  @Override
  public String toString() {
    return Names.toString(this);
  }

  public static Name fromString(String str) {
    final Matcher matcher = STR_REPR.matcher(str);
    Preconditions.checkArgument(matcher.matches());

    final String ns = matcher.group(1);
    return new RelationalName(Strings.isNullOrEmpty(ns) ? null : URI.create(ns), matcher.group(2));
  }

  public int compareTo(Name o) {
    return Names.COMPARATOR.compare(this, o);
  }
}
