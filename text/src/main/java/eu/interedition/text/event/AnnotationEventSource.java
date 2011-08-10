package eu.interedition.text.event;

import com.google.common.base.Predicates;
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
    textRepository.read(text, new TextRepository.TextReader() {

      public void read(Reader content, int contentLength) throws IOException {
        final SortedMap<Integer, Set<Annotation>> starts = Maps.newTreeMap();
        final SortedMap<Integer, Set<Annotation>> ends = Maps.newTreeMap();

        int offset = 0;
        int next = 0;
        int pageEnd = 0;

        listener.start();

        final Map<Annotation, Map<QName, String>> annotationData = Maps.newHashMap();
        while (true) {
          if ((offset % pageSize) == 0) {
            pageEnd = Math.min(offset + pageSize, contentLength);
            final Range pageRange = new Range(offset, pageEnd);
            final Iterable<Annotation> pageAnnotations = annotationRepository.find(and(criterion, text(text), rangeOverlap(pageRange)));
            final Map<Annotation, Map<QName, String>> pageAnnotationData = annotationRepository.get(pageAnnotations, Collections.<QName>emptySet());
            for (Annotation a : pageAnnotations) {
              final int start = a.getRange().getStart();
              final int end = a.getRange().getEnd();
              if (start >= offset) {
                Set<Annotation> starting = starts.get(start);
                if (starting == null) {
                  starts.put(start, starting = Sets.newHashSet());
                }
                starting.add(a);
                annotationData.put(a, pageAnnotationData.get(a));
              }
              if (end <= pageEnd) {
                Set<Annotation> ending = ends.get(end);
                if (ending == null) {
                  ends.put(end, ending = Sets.newHashSet());
                }
                ending.add(a);
                annotationData.put(a, pageAnnotationData.get(a));
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

          final int readTo = Math.min(pageEnd, next);
          if (offset < readTo) {
            final char[] currentText = new char[readTo - offset];
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

  protected static Map<Annotation, Map<QName, String>> filter(Map<Annotation, Map<QName, String>> data, Set<Annotation> keys, boolean remove) {
    final Map<Annotation, Map<QName, String>> filtered = Maps.newHashMap();
    for (Iterator<Map.Entry<Annotation, Map<QName, String>>> it = data.entrySet().iterator(); it.hasNext();  ) {
      final Map.Entry<Annotation, Map<QName, String>> annotationEntry = it.next();
      final Annotation annotation = annotationEntry.getKey();
      if (keys.contains(annotation)) {
        filtered.put(annotation, annotationEntry.getValue());
        if (remove) {
          it.remove();
        }
      }
    }
    return filtered;
  }
}
