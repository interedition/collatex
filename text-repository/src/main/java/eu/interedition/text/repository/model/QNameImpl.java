/*
 * #%L
 * Text Repository: Datastore for texts based on Interedition's model.
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
package eu.interedition.text.repository.model;

import com.google.common.base.Function;
import eu.interedition.text.QName;
import eu.interedition.text.rdbms.RelationalQName;
import eu.interedition.text.util.QNames;
import org.codehaus.jackson.annotate.JsonProperty;

import java.net.URI;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class QNameImpl implements QName {
  private String id;
  private URI namespaceURI;
  private String localName;

  public QNameImpl() {
  }

  public QNameImpl(QName name) {
    setNamespaceURI(name.getNamespaceURI());
    setLocalName(name.getLocalName());
    if (name instanceof RelationalQName) {
      setId(Long.toString(((RelationalQName) name).getId()));
    }
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  @JsonProperty("ns")
  @Override
  public URI getNamespaceURI() {
    return namespaceURI;
  }

  @JsonProperty("ns")
  public void setNamespaceURI(URI namespaceURI) {
    this.namespaceURI = namespaceURI;
  }

  @JsonProperty("n")
  @Override
  public String getLocalName() {
    return localName;
  }

  @JsonProperty("n")
  public void setLocalName(String localName) {
    this.localName = localName;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof QName) {
      return QNames.equal(this, (QName) obj);
    }
    return super.equals(obj);
  }

  @Override
  public int hashCode() {
    return QNames.hashCode(this);
  }

  @Override
  public int compareTo(QName o) {
    return QNames.COMPARATOR.compare(this, o);
  }

  public static final Function<QName, QNameImpl> TO_BEAN = new Function<QName, QNameImpl>() {
    @Override
    public QNameImpl apply(QName input) {
      return new QNameImpl(input);
    }
  };
}
