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
import eu.interedition.text.Text;
import eu.interedition.text.mem.SimpleText;

public class RelationalText extends SimpleText {
  protected long id;

  public RelationalText(Type type, long length, byte[] digest, long id) {
    super(type, length, digest);
    this.id = id;
  }

  public RelationalText(Type type, String content, long id) {
    super(type, content);
    this.id = id;
  }

  public RelationalText(RelationalText other) {
    this(other.type, other.length, other.digest, other.id);
  }

  public long getId() {
    return id;
  }

  @Override
  public String toString() {
    return toStringHelper().add("id", Long.toString(id)).toString();
  }

  @Override
  public boolean equals(Object obj) {
    if (id != 0 && obj != null && obj instanceof RelationalText) {
      return id == ((RelationalText) obj).id;
    }
    return super.equals(obj);
  }

  @Override
  public int hashCode() {
    return (id == 0 ? super.hashCode() : Objects.hashCode(id));
  }

}
