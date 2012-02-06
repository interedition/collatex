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
import eu.interedition.text.Annotation;
import eu.interedition.text.Text;
import eu.interedition.text.util.TextDigestingFilterReader;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class SimpleText implements Text {
  protected final Annotation layer;
  protected final Type type;
  protected final long length;
  protected final byte[] digest;
  protected final String content;

  public SimpleText(Annotation layer, Type type, long length, byte[] digest) {
    this(layer, type, length, digest, null);
  }

  public SimpleText(Annotation layer, Type type, long length, byte[] digest, String content) {
    this.layer = layer;
    this.type = type;
    this.length = length;
    this.digest = digest;
    this.content = content;
  }

  public SimpleText(Annotation layer, Type type, String content) {
    this(layer, type, content.length(), TextDigestingFilterReader.digest(content), content);
  }

  @Override
  public Annotation getLayer() {
    return layer;
  }

  @Override
  public Type getType() {
    return type;
  }

  @Override
  public long getLength() {
    return length;
  }

  @Override
  public byte[] getDigest() {
    return digest;
  }

  public String getContent() {
    return content;
  }

  protected Objects.ToStringHelper toStringHelper() {
    return Objects.toStringHelper(this).addValue(layer).add("type", type).add("length", length);
  }

  @Override
  public String toString() {
    return toStringHelper().toString();
  }
}
