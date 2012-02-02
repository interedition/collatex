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
package eu.interedition.text.event;

import com.google.common.base.Joiner;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import eu.interedition.text.*;
import eu.interedition.text.query.Criterion;

import java.io.IOException;
import java.io.Reader;
import java.util.*;

import static eu.interedition.text.query.Criteria.*;

public class AnnotationEventSource {
  private AnnotationRepository annotationRepository;
  private TextRepository textRepository;

  private static final com.google.common.base.Predicate<Annotation> EMPTY = new com.google.common.base.Predicate<Annotation>() {
    public boolean apply(Annotation input) {
      return input.getRange().length() == 0;
    }
  };

  public AnnotationEventSource() {
  }

  public void setAnnotationRepository(AnnotationRepository annotationRepository) {
    this.annotationRepository = annotationRepository;
  }

  public void setTextRepository(TextRepository textRepository) {
    this.textRepository = textRepository;
  }

  public void listen(final AnnotationEventListener listener, final Text text, final Criterion criterion) throws IOException {
    listen(listener, Integer.MAX_VALUE, text, criterion);
  }

  public void listen(final AnnotationEventListener listener, final int pageSize, final Text text, final Criterion criterion) throws IOException {
    textRepository.read(text, new TextConsumer() {

      public void read(Reader content, long contentLength) throws IOException {
        final SortedMap<Long, Set<Annotation>> starts = Maps.newTreeMap();
        final SortedMap<Long, Set<Annotation>> ends = Maps.newTreeMap();

        long offset = 0;
        long next = 0;
        long pageEnd = 0;

        listener.start();

        final Set<Annotation> annotationData = Sets.newHashSet();
        while (true) {
          if ((offset % pageSize) == 0) {
            pageEnd = Math.min(offset + pageSize, contentLength);
            final Range pageRange = new Range(offset, pageEnd);
            final Iterable<Annotation> pageAnnotations = annotationRepository.find(and(criterion, text(text), rangeOverlap(pageRange)));
            for (Annotation a : pageAnnotations) {
              final long start = a.getRange().getStart();
              final long end = a.getRange().getEnd();
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

            next = Math.min(starts.isEmpty() ? contentLength : starts.firstKey(), ends.isEmpty() ? contentLength : ends.firstKey());
          }

          if (offset == next) {
            final Set<Annotation> startEvents = (!starts.isEmpty() && offset == starts.firstKey() ? starts.remove(starts.firstKey()) : Sets.<Annotation>newHashSet());
            final Set<Annotation> endEvents = (!ends.isEmpty() && offset == ends.firstKey() ? ends.remove(ends.firstKey()) : Sets.<Annotation>newHashSet());

            final Set<Annotation> terminating = Sets.filter(endEvents, Predicates.not(EMPTY));
            if (!terminating.isEmpty()) listener.end(offset, filter(annotationData, terminating, true));

            final Set<Annotation> empty = Sets.filter(startEvents, EMPTY);
            if (!empty.isEmpty()) listener.empty(offset, filter(annotationData, empty, true));

            final Set<Annotation> starting = Sets.filter(startEvents, Predicates.not(EMPTY));
            if (!starting.isEmpty()) listener.start(offset, filter(annotationData, starting, false));


            next = Math.min(starts.isEmpty() ? contentLength : starts.firstKey(), ends.isEmpty() ? contentLength : ends.firstKey());
          }

          if (offset == contentLength) {
            break;
          }

          final long readTo = Math.min(pageEnd, next);
          if (offset < readTo) {
            final char[] currentText = new char[(int) (readTo - offset)];
            int read = content.read(currentText);
            if (read > 0) {
              listener.text(new Range(offset, offset + read), new String(currentText, 0, read));
              offset += read;
            }
          }
        }

        listener.end();
      }
    });
  }

  protected static Iterable<Annotation> filter(Iterable<Annotation> data, Set<Annotation> keys, boolean remove) {
    final List<Annotation> filtered = Lists.newArrayList();
    for (Iterator<Annotation> it = data.iterator(); it.hasNext();  ) {
      final Annotation annotation = it.next();
      if (keys.contains(annotation)) {
        filtered.add(annotation);
        if (remove) {
          it.remove();
        }
      }
    }
    return filtered;
  }
}
