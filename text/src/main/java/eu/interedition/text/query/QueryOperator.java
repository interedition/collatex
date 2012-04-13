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

import com.google.common.collect.Lists;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Junction;

import java.util.List;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public abstract class QueryOperator extends QueryCriterion {
  protected final List<QueryCriterion> operands = Lists.newLinkedList();

  QueryOperator() {
  }

  public QueryOperator add(QueryCriterion criterion) {
    criterion.parent = this;
    operands.add(criterion);
    return this;
  }

  @Override
  Criterion restrict() {
    final Junction junction = junction();
    for (QueryCriterion operand : operands) {
      junction.add(operand.restrict());
    }
    return junction;
  }

  abstract Junction junction();
}
