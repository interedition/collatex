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

import com.google.common.base.Objects;
import eu.interedition.text.AnnotationLink;
import eu.interedition.text.Name;
import eu.interedition.text.mem.SimpleAnnotationLink;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class RelationalAnnotationLink extends SimpleAnnotationLink {
  protected long id;

  public RelationalAnnotationLink(Name name, long id) {
    super(name);
    this.id = id;
  }

  public long getId() {
    return id;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj != null && obj instanceof RelationalAnnotationLink) {
      return id == ((RelationalAnnotationLink)obj).id;
    }
    return super.equals(obj);
  }

  @Override
  public String toString() {
    return toStringHelper().addValue(id).toString();
  }
}
