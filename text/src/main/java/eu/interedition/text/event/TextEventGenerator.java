package eu.interedition.text.event;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.*;
import eu.interedition.text.*;

import java.io.IOException;
import java.io.Reader;
import java.util.*;

import static java.util.Collections.singleton;

public class TextEventGenerator {
  private QNameRepository nameRepository;
  private AnnotationRepository annotationRepository;
  private TextRepository textRepository;

  private static final Predicate<Annotation> EMPTY = new Predicate<Annotation>() {
    public boolean apply(Annotation input) {
      return input.getRange().length() == 0;
    }
  };

  public TextEventGenerator() {
  }

  public void setNameRepository(QNameRepository nameRepository) {
    this.nameRepository = nameRepository;
  }

  public void setAnnotationRepository(AnnotationRepository annotationRepository) {
    this.annotationRepository = annotationRepository;
  }

  public void setTextRepository(TextRepository textRepository) {
    this.textRepository = textRepository;
  }

  public void generate(final TextEventListener listener, final Text text) throws IOException {
    generate(listener, text, null);
  }

  public void generate(final TextEventListener listener, final Text text, final Set<QName> names) throws IOException {
    generate(listener, text, names, Integer.MAX_VALUE);
  }

  public void generate(final TextEventListener listener, final Text text, final Set<QName> names, final int pageSize) throws IOException {
    textRepository.read(text, new TextContentReader() {

      public void read(Reader content, int contentLength) throws IOException {
        final SortedMap<Integer, Set<Annotation>> starts = Maps.newTreeMap();
        final SortedMap<Integer, Set<Annotation>> ends = Maps.newTreeMap();

        int offset = 0;
        int next = 0;
        int pageEnd = 0;

        listener.start();

        while (true) {
          if ((offset % pageSize) == 0) {
            pageEnd = Math.min(offset + pageSize, contentLength);
            final Range pageRange = new Range(offset, pageEnd);
            for (Annotation a : annotationRepository.find(text, names, singleton(pageRange))) {
              final int start = a.getRange().getStart();
              final int end = a.getRange().getEnd();
              if (start >= offset) {
                Set<Annotation> annotations = starts.get(start);
                if (annotations == null) {
                  starts.put(start, annotations = Sets.newHashSet());
                }
                annotations.add(a);
              }
              if (end <= pageEnd) {
                Set<Annotation> annotations = ends.get(end);
                if (annotations == null) {
                  ends.put(end, annotations = Sets.newHashSet());
                }
                annotations.add(a);
              }
            }

            next = Math.min(starts.isEmpty() ? contentLength : starts.firstKey(), ends.isEmpty() ? contentLength : ends.firstKey());
          }

          if (offset == next) {
            final Set<Annotation> startEvents = (!starts.isEmpty() && offset == starts.firstKey() ? starts.remove(starts.firstKey()) : Sets.<Annotation>newHashSet());
            final Set<Annotation> endEvents = (!ends.isEmpty() && offset == ends.firstKey() ? ends.remove(ends.firstKey()) : Sets.<Annotation>newHashSet());

            final Set<Annotation> terminating = Sets.filter(endEvents, Predicates.not(EMPTY));
            if (!terminating.isEmpty()) listener.end(offset, terminating);

            final Set<Annotation> empty = Sets.filter(startEvents, EMPTY);
            if (!empty.isEmpty()) listener.empty(offset, empty);

            final Set<Annotation> starting = Sets.filter(startEvents, Predicates.not(EMPTY));
            if (!starting.isEmpty()) listener.start(offset, starting);


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
}
