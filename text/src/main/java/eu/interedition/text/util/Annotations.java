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
package eu.interedition.text.util;

import com.google.common.base.Function;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;
import eu.interedition.text.Annotation;
import eu.interedition.text.Name;
import eu.interedition.text.Text;

import java.util.Comparator;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class Annotations {
  public static Ordering<Annotation> DEFAULT_ORDERING = Ordering.from(new Comparator<Annotation>() {
    public int compare(Annotation o1, Annotation o2) {
      return o1.compareTo(o2);
    }
  });

  public static ComparisonChain compare(Annotation a, Annotation b) {
    return ComparisonChain.start()
            .compare(a.getRange(), b.getRange())
            .compare(a.getName(), b.getName());
  }

  public static final Function<Annotation, Name> NAME = new Function<Annotation, Name>() {
    public Name apply(Annotation input) {
      return input.getName();
    }
  };

  public static final Function<Annotation, Text> TEXT = new Function<Annotation, Text>() {

    @Override
    public Text apply(Annotation input) {
      return input.getText();
    }
  };
}
