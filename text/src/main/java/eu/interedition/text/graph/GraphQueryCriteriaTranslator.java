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
package eu.interedition.text.graph;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import eu.interedition.text.QName;
import eu.interedition.text.QNameRepository;
import eu.interedition.text.Range;
import eu.interedition.text.query.*;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.neo4j.graphdb.Node;

import java.net.URI;
import java.util.List;

import static org.apache.lucene.search.BooleanClause.Occur.MUST;
import static org.apache.lucene.search.BooleanClause.Occur.SHOULD;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class GraphQueryCriteriaTranslator {

  protected Query toQuery(Criterion criterion) {
    if (criterion instanceof Operator) {
      return toQuery((Operator) criterion);
    } else if (criterion instanceof AnnotationNameCriterion) {
      return toQuery((AnnotationNameCriterion) criterion);
    } else if (criterion instanceof TextCriterion) {
      return toQuery((TextCriterion) criterion);
    } else if (criterion instanceof AnnotationLinkNameCriterion) {
      return toQuery((AnnotationLinkNameCriterion) criterion);
    } else if (criterion instanceof RangeOverlapCriterion) {
      return toQuery((RangeOverlapCriterion) criterion);
    } else if (criterion instanceof RangeLengthCriterion) {
      return toQuery((RangeLengthCriterion) criterion);
    } else if (criterion instanceof AnnotationIdentityCriterion) {
      return toQuery((AnnotationIdentityCriterion) criterion);
    } else if (criterion instanceof AnyCriterion) {
      return toQuery((AnyCriterion) criterion);
    } else if (criterion instanceof NoneCriterion) {
      return toQuery((NoneCriterion) criterion);
    } else {
      throw new IllegalArgumentException(Objects.toStringHelper(criterion).toString());
    }
  }

  protected Query toQuery(Operator op) {
    final List<Criterion> operands = op.getOperands();
    Preconditions.checkArgument(!operands.isEmpty());

    if (operands.size() < 2) {
      return toQuery(operands.get(0));
    }

    final BooleanQuery query = new BooleanQuery();
    BooleanClause.Occur occur = (op instanceof AndOperator ? MUST : SHOULD);
    for (Criterion c : operands) {
      query.add(toQuery(c), occur);
    }
    return query;
  }

  protected Query toQuery(AnnotationLinkNameCriterion criterion) {
    throw new UnsupportedOperationException();
  }

  protected Query toQuery(AnnotationIdentityCriterion criterion) {
    final Node annotationNode = ((GraphAnnotation) criterion.getAnnotation()).getNode();
    return new TermQuery(new Term(GraphAnnotation.PROP_ID, Long.toString(annotationNode.getId())));
  }

  protected Query toQuery(AnnotationNameCriterion criterion) {
    final QName name = criterion.getName();
    final URI namespaceURI = name.getNamespaceURI();

    final BooleanQuery query = new BooleanQuery();
    query.add(new TermQuery(new Term(GraphQName.PROP_NS, (namespaceURI == null ? "" : namespaceURI.toString()))), MUST);
    query.add(new TermQuery(new Term(GraphQName.PROP_LOCAL_NAME, name.getLocalName())), MUST);
    return query;
  }

  protected Query toQuery(TextCriterion criterion) {
    return new TermQuery(new Term(GraphAnnotation.PROP_TEXT, Long.toString(((GraphText)criterion.getText()).getNode().getId())));
  }

  protected Query toQuery(RangeOverlapCriterion criterion) {
    final Range range = criterion.getRange();

    final BooleanQuery query = new BooleanQuery();
    query.add(NumericRangeQuery.newLongRange(GraphAnnotation.PROP_RANGE_START, 0L, range.getEnd(), true, false), MUST);
    query.add(NumericRangeQuery.newLongRange(GraphAnnotation.PROP_RANGE_END, range.getStart(), Long.MAX_VALUE, false, true), MUST);
    return query;
  }

  protected Query toQuery(RangeLengthCriterion criterion) {
    return new TermQuery(new Term(GraphAnnotation.PROP_RANGE_LENGTH, Long.toString(criterion.getLength())));
  }

  protected Query toQuery(AnyCriterion criterion) {
    throw new UnsupportedOperationException();
  }

  protected Query toQuery(NoneCriterion criterion) {
    throw new UnsupportedOperationException();
  }


}
