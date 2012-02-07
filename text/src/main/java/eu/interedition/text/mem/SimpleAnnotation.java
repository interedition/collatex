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
import com.google.common.base.Throwables;
import com.google.common.collect.Ordering;
import eu.interedition.text.Annotation;
import eu.interedition.text.Name;
import eu.interedition.text.Range;
import eu.interedition.text.Text;
import eu.interedition.text.util.Annotations;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class SimpleAnnotation implements Annotation {
  private static final Ordering<Annotation> ORDERING = Annotations.BASIC_ORDERING.compound(Ordering.arbitrary());

  public static final ObjectMapper JSON = new ObjectMapper();

  protected static final JsonNode EMPTY_DATA_NODE = JSON.createObjectNode();
  protected static final byte[] EMPTY_DATA = new byte[0];

  protected final Text text;
  protected final Name name;
  protected final Range range;
  protected final byte[] data;

  protected JsonNode dataNode;

  public SimpleAnnotation(Text text, Name name, Range range, byte[] data) {
    this.text = text;
    this.name = name;
    this.range = range;
    this.data = (data == null ? EMPTY_DATA : data);
  }

  public SimpleAnnotation(Text text, Name name, Range range) {
    this(text, name, range, (byte[]) null);
  }

  public SimpleAnnotation(Text text, Name name, Range range, JsonNode data) {
    this(text, name, range, toData(data));
  }
  
  public SimpleAnnotation(Annotation other) {
    this.text = other.getText();
    this.name = other.getName();
    this.range = other.getRange();
    this.data = toRawData(other);
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
  public JsonNode getData() {
    if (dataNode == null) {
      dataNode = toDataNode(data);
    }
    return dataNode;
  }

  public static byte[] toRawData(Annotation a) {
    return (a instanceof SimpleAnnotation ? ((SimpleAnnotation) a).data : toData(a.getData()));
  }

  public static JsonNode toDataNode(byte[] data) {
    try {
      return (data == null || data.length == 0 ? EMPTY_DATA_NODE : JSON.readTree(new ByteArrayInputStream(data)));
    } catch (IOException e) {
      throw Throwables.propagate(e);
    }
  }

  public static byte[] toData(JsonNode data) {
    try {
      return (data == null ? EMPTY_DATA : JSON.writeValueAsBytes(data));
    } catch (IOException e) {
      throw Throwables.propagate(e);
    }
  }

  protected Objects.ToStringHelper toStringHelper() {
    return Objects.toStringHelper(this).addValue(text).addValue(getName()).addValue(getRange()).addValue(getData());
  }

  @Override
  public String toString() {
    return toStringHelper().toString();
  }

  public int compareTo(Annotation o) {
    return ORDERING.compare(this, o);
  }
}
