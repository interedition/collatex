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
import eu.interedition.text.mem.SimpleName;
import eu.interedition.text.util.Names;

import javax.xml.namespace.QName;
import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A qualified/ "namespaced" identifier.
 *
 * @author <a href="http://gregor.middell.net/" title="Homepage of Gregor Middell">Gregor Middell</a>
 */
public class RelationalName extends SimpleName {
  protected final long id;

  public RelationalName(URI namespace, String localName, long id) {
    super(namespace, localName);
    this.id = id;
  }

  public RelationalName(String namespace, String localName, long id) {
    super(namespace, localName);
    this.id = id;
  }

  public RelationalName(Name name, long id) {
    this(name.getNamespace(), name.getLocalName(), id);
  }

  public RelationalName(QName name, long id) {
    super(name);
    this.id = id;
  }

  public long getId() {
    return id;
  }
}
