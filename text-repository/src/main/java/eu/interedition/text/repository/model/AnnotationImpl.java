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
import com.google.common.collect.Ordering;
import eu.interedition.text.Annotation;
import eu.interedition.text.Range;
import eu.interedition.text.Text;
import eu.interedition.text.repository.conversion.RangeDeserializer;
import eu.interedition.text.repository.conversion.RangeSerializer;
import eu.interedition.text.util.Annotations;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class AnnotationImpl implements Annotation {
  private NameImpl name;
  private Range range;

  public AnnotationImpl() {
  }

  public AnnotationImpl(NameImpl name, Range range) {
    this.name = name;
    this.range = range;
  }


  @JsonIgnore
  @Override
  public Text getText() {
    return null;
  }

  @JsonProperty("n")
  public NameImpl getName() {
    return name;
  }

  @JsonProperty("n")
  public void setName(NameImpl name) {
    this.name = name;
  }

  @JsonProperty("r")
  @JsonSerialize(using = RangeSerializer.class)
  public Range getRange() {
    return range;
  }

  @JsonProperty("r")
  @JsonDeserialize(using = RangeDeserializer.class)
  public void setRange(Range range) {
    this.range = range;
  }

  public static final Function<Annotation, AnnotationImpl> TO_BEAN = new Function<Annotation, AnnotationImpl>() {
    @Override
    public AnnotationImpl apply(Annotation input) {
      return new AnnotationImpl(NameImpl.TO_BEAN.apply(input.getName()), input.getRange());
    }
  };

  @Override
  public int compareTo(Annotation o) {
    return Annotations.compare(this, o).compare(this, o, Ordering.arbitrary()).result();
  }
}
