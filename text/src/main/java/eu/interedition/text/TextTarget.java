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
package eu.interedition.text;

import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import org.hibernate.annotations.Index;

import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage of Gregor Middell">Gregor Middell</a>
 */
@Embeddable
public class TextTarget extends TextRange {

  private Text text;

  public TextTarget() {
  }

  public TextTarget(Text text, TextRange range) {
    this(text, range.getStart(), range.getEnd());
  }

  public TextTarget(Text text, long start, long end) {
    super(start, end);
    this.text = text;
  }

  /**
   * Copy constructor.
   *
   * @param b the segment address to be copied
   */
  public TextTarget(TextTarget b) {
    this(b.text, b.start, b.end);
  }

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "text_id", nullable = false)
  public Text getText() {
    return text;
  }

  public void setText(Text text) {
    this.text = text;
  }

  @Override
  @Column(name = "text_start", nullable = false)
  @Index(name = "text_start_end", columnNames = {"text_start, text_end"})
  public long getStart() {
    return super.getStart();
  }

  @Override
  @Column(name = "text_end", nullable = false)
  public long getEnd() {
    return super.getEnd();
  }

  @Column(name = "text_length", nullable = false)
  public long getLength() {
    return length();
  }

  public void setLength(long length) {
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(start, end, text);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof TextTarget)) {
      return super.equals(obj);
    }

    TextTarget b = (TextTarget) obj;
    return (this.start == b.start) && (this.end == b.end) && (this.text == b.text);
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this).addValue(text).addValue(toString(start, end)).toString();
  }

  public static Predicate<TextTarget> of(final Text text) {
    return new Predicate<TextTarget>() {
      @Override
      public boolean apply(@Nullable TextTarget input) {
        return text.equals(input.getText());
      }
    };
  }
}
