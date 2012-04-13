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

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.Closeables;
import eu.interedition.text.Annotation;
import eu.interedition.text.Text;
import eu.interedition.text.TextRange;
import eu.interedition.text.TextTarget;
import eu.interedition.text.util.SQL;
import org.hibernate.*;
import org.hibernate.criterion.Criterion;
import org.hibernate.sql.JoinType;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;

import static eu.interedition.text.query.QueryCriteria.and;
import static eu.interedition.text.query.QueryCriteria.rangeOverlap;
import static eu.interedition.text.query.QueryCriteria.text;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public abstract class QueryCriterion {
  private static int DEFAULT_PAGE_SIZE = 512000;

  QueryOperator parent;

  QueryCriterion getRoot() {
    QueryCriterion root = this;
    while (parent != null) {
      root = this.parent;
    }
    return root;
  }

  public Iterable<Annotation> iterate(Session session) {
    return SQL.iterate(criteria(session), Annotation.class);
  }

  public void delete(Session session) {
    for (Annotation annotation : iterate(session)) {
      session.delete(annotation);
    }
  }

  public void listen(Session session, final Text text, final int pageSize, final AnnotationListener listener) throws IOException {
    final long contentLength = text.getLength();
    Reader contentStream = null;
    try {
      contentStream = text.read().getInput();
      final SortedMap<Long, Set<Annotation>> starts = Maps.newTreeMap();
      final SortedMap<Long, Set<Annotation>> ends = Maps.newTreeMap();

      long offset = 0;
      long next = 0;
      long pageEnd = 0;

      listener.start(contentLength);

      final Set<Annotation> annotationData = Sets.newHashSet();
      while (true) {
        if ((offset % pageSize) == 0) {
          pageEnd = Math.min(offset + pageSize, contentLength);
          final TextRange pageRange = new TextRange(offset, pageEnd);
          final Iterable<Annotation> pageAnnotations = and(this, text(text), rangeOverlap(pageRange)).iterate(session);
          for (Annotation a : pageAnnotations) {
            for (TextTarget target : a.getTargets()) {
              if (!text.equals(target.getText())) {
                continue;
              }
              final long start = target.getStart();
              final long end = target.getEnd();
              if (start >= offset) {
                Set<Annotation> starting = starts.get(start);
                if (starting == null) {
                  starts.put(start, starting = Sets.newHashSet());
                }
                starting.add(a);
                annotationData.add(a);
              }
              if (end <= pageEnd) {
                Set<Annotation> ending = ends.get(end);
                if (ending == null) {
                  ends.put(end, ending = Sets.newHashSet());
                }
                ending.add(a);
                annotationData.add(a);
              }
            }
          }

          next = Math.min(starts.isEmpty() ? contentLength : starts.firstKey(), ends.isEmpty() ? contentLength : ends.firstKey());
        }

        if (offset == next) {
          final Set<Annotation> startEvents = (!starts.isEmpty() && offset == starts.firstKey() ? starts.remove(starts.firstKey()) : Sets.<Annotation>newHashSet());
          final Set<Annotation> endEvents = (!ends.isEmpty() && offset == ends.firstKey() ? ends.remove(ends.firstKey()) : Sets.<Annotation>newHashSet());

          final Set<Annotation> emptyEvents = Sets.newHashSet(Sets.filter(endEvents, emptyIn(text)));
          endEvents.removeAll(emptyEvents);

          if (!endEvents.isEmpty()) listener.end(offset, filter(annotationData, endEvents, true));
          if (!startEvents.isEmpty()) listener.start(offset, filter(annotationData, startEvents, false));
          if (!emptyEvents.isEmpty()) listener.end(offset, filter(annotationData, emptyEvents, true));

          next = Math.min(starts.isEmpty() ? contentLength : starts.firstKey(), ends.isEmpty() ? contentLength : ends.firstKey());
        }

        if (offset == contentLength) {
          break;
        }

        final long readTo = Math.min(pageEnd, next);
        if (offset < readTo) {
          final char[] currentText = new char[(int) (readTo - offset)];
          int read = contentStream.read(currentText);
          if (read > 0) {
            listener.text(new TextRange(offset, offset + read), new String(currentText, 0, read));
            offset += read;
          }
        }
      }

      listener.end();
    } finally {
      Closeables.close(contentStream, false);
    }
  }

  public Criteria criteria(Session session) {
    final Criteria c = session.createCriteria(Annotation.class);
    c.createAlias("name", "name");
    c.createCriteria("targets", "target").createCriteria("text", "text").createAlias("layer", "layer", JoinType.LEFT_OUTER_JOIN);
    return c.add(getRoot().restrict());
  }

  private Predicate<Annotation> emptyIn(final Text text) {
    return new Predicate<Annotation>() {
      @Override
      public boolean apply(@Nullable Annotation input) {
        for (TextTarget target : input.getTargets()) {
          if (target.length() == 0 && text.equals(target.getText())) {
            return true;
          }
        }
        return false;
      }
    };
  }

  public void listen(Session session, final Text text, final AnnotationListener listener) throws IOException {
    listen(session, text, DEFAULT_PAGE_SIZE, listener);
  }

  protected static Iterable<Annotation> filter(Iterable<Annotation> annotations, Set<Annotation> toFind, boolean remove) {
    final List<Annotation> filtered = Lists.newArrayList();
    for (Iterator<Annotation> it = annotations.iterator(); it.hasNext();  ) {
      final Annotation annotation = it.next();
      if (toFind.contains(annotation)) {
        filtered.add(annotation);
        if (remove) {
          it.remove();
        }
      }
    }
    return filtered;
  }

  abstract Criterion restrict();
}
