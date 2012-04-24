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
import eu.interedition.text.Name;
import eu.interedition.text.TextRange;
import eu.interedition.text.Text;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class QueryCriteria {
  public static QueryCriterion any() {
    return new AnyCriterion();
  }

  public static QueryCriterion none() {
    return new NoneCriterion();
  }

  public static QueryCriterion is(Annotation annotation) {
    return new AnnotationIdentityCriterion(annotation);
  }

  public static QueryCriterion annotationName(Name name) {
    return new AnnotationNameCriterion(name);
  }

  public static QueryCriterion text(Text text) {
    return new AnnotationTargetTextCriterion(text);
  }

  public static QueryCriterion rangeOverlap(TextRange range) {
    return new RangeOverlapCriterion(range);
  }

  public static QueryCriterion rangeFitsWithin(TextRange range) {
    return new RangeFitsWithinCriterion(range);
  }

  public static QueryCriterion rangeLength(int length) {
    return new RangeLengthCriterion(length);
  }

  public static QueryOperator and(QueryCriterion... criteria) {
    final AndQueryOperator andOperator = new AndQueryOperator();
    for (QueryCriterion criterion : criteria) {
      andOperator.add(criterion);
    }
    return andOperator;
  }

  public static QueryOperator or(QueryCriterion... criteria) {
    final OrQueryOperator orOperator = new OrQueryOperator();
    for (QueryCriterion criterion : criteria) {
      orOperator.add(criterion);
    }
    return orOperator;
  }
}
