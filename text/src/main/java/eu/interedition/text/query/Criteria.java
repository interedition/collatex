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
package eu.interedition.text.query;

import eu.interedition.text.Annotation;
import eu.interedition.text.QName;
import eu.interedition.text.Range;
import eu.interedition.text.Text;

import java.util.Arrays;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class Criteria {
  public static Criterion any() {
    return new AnyCriterion();
  }

  public static Criterion none() {
    return new NoneCriterion();
  }

  public static Criterion is(Annotation annotation) {
    return new AnnotationIdentityCriterion(annotation);
  }

  public static Criterion annotationName(QName name) {
    return new AnnotationNameCriterion(name);
  }

  public static Criterion linkName(QName name) {
    return new AnnotationLinkNameCriterion(name);
  }

  public static Criterion text(Text text) {
    return new TextCriterion(text);
  }

  public static Criterion rangeOverlap(Range range) {
    return new RangeOverlapCriterion(range);
  }

  public static Criterion rangeLength(int length) {
    return new RangeLengthCriterion(length);
  }

  public static Operator and(Criterion... criteria) {
    return new AndOperator(Arrays.asList(criteria));
  }

  public static Operator or(Criterion... criteria) {
    return new OrOperator(Arrays.asList(criteria));
  }

  public static Criterion not(Criterion criterion) {
    return new NotOperator(criterion);
  }
}
