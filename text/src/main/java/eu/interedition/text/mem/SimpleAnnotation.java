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

import com.google.common.base.Objects;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import eu.interedition.text.*;
import eu.interedition.text.util.Annotations;

import javax.xml.namespace.QName;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class SimpleAnnotation implements Annotation {
  protected final Text text;
  protected final Name name;
  protected final Range range;
  protected final Map<Name, String> data;

  public SimpleAnnotation(Text text, Name name, Range range, Map<Name, String> data) {
    this.text = text;
    this.name = name;
    this.range = range;
    this.data = (data == null || data.isEmpty() ? null : Collections.unmodifiableMap(data));
  }

  public SimpleAnnotation(Annotation other) {
    this(other.getText(), other.getName(), other.getRange(), other.getData());
  }

  public Text getText() {
    return text;
  }

  public Name getName() {
    return name;
  }

  public Range getRange() {
    return range;
  }

  @Override
  public Map<Name, String> getData() {
    return (data == null ? Collections.<Name, String>emptyMap() : data);
  }

  protected Objects.ToStringHelper toStringHelper() {
    return Objects.toStringHelper(this).addValue(text).addValue(getName()).addValue(getRange());
  }

  @Override
  public String toString() {
    return toStringHelper().toString();
  }

  public int compareTo(Annotation o) {
    return Annotations.compare(this, o).compare(this, o, Ordering.arbitrary()).result();
  }
}
