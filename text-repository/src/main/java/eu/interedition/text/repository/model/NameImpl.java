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
import eu.interedition.text.Name;
import eu.interedition.text.rdbms.RelationalName;
import eu.interedition.text.util.Names;
import org.codehaus.jackson.annotate.JsonProperty;

import java.net.URI;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class NameImpl implements Name {
  private String id;
  private URI namespaceURI;
  private String localName;

  public NameImpl() {
  }

  public NameImpl(Name name) {
    setNamespaceURI(name.getNamespace());
    setLocalName(name.getLocalName());
    if (name instanceof RelationalName) {
      setId(Long.toString(((RelationalName) name).getId()));
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
  public URI getNamespace() {
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
    if (obj instanceof Name) {
      return Names.equal(this, (Name) obj);
    }
    return super.equals(obj);
  }

  @Override
  public int hashCode() {
    return Names.hashCode(this);
  }

  @Override
  public int compareTo(Name o) {
    return Names.COMPARATOR.compare(this, o);
  }

  public static final Function<Name, NameImpl> TO_BEAN = new Function<Name, NameImpl>() {
    @Override
    public NameImpl apply(Name input) {
      return new NameImpl(input);
    }
  };
}
