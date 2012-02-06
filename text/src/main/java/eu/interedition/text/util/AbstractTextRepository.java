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
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.Closeables;
import com.google.common.io.FileBackedOutputStream;
import eu.interedition.text.Annotation;
import eu.interedition.text.AnnotationConsumer;
import eu.interedition.text.Name;
import eu.interedition.text.Range;
import eu.interedition.text.Text;
import eu.interedition.text.TextConsumer;
import eu.interedition.text.TextListener;
import eu.interedition.text.TextRepository;
import eu.interedition.text.mem.SimpleName;
import eu.interedition.text.query.Criterion;
import eu.interedition.text.xml.XML;
import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.stax2.XMLOutputFactory2;

import javax.xml.stream.*;
import java.io.*;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;

import static com.google.common.base.Functions.compose;
import static com.google.common.collect.Iterables.getOnlyElement;
import static eu.interedition.text.query.Criteria.and;
import static eu.interedition.text.query.Criteria.rangeOverlap;
import static eu.interedition.text.query.Criteria.text;
import static eu.interedition.text.transform.AnnotationTransformers.adopt;
import static java.util.Collections.singleton;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public abstract class AbstractTextRepository implements TextRepository {
  protected final XMLInputFactory2 xmlInputFactory = XML.createXMLInputFactory();
  protected final XMLOutputFactory2 xmlOutputFactory = XML.createXMLOutputFactory();

  private int memoryBufferThreshold = 1001024;
  protected int batchSize = 10000;
  protected int defaultPageSize = 512000;

  public void setMemoryBufferThreshold(int memoryBufferThreshold) {
    this.memoryBufferThreshold = memoryBufferThreshold;
  }


  public void setBatchSize(int batchSize) {
    this.batchSize = batchSize;
  }

  public void setDefaultPageSize(int defaultPageSize) {
    this.defaultPageSize = defaultPageSize;
  }

  public Text create(Annotation layer, XMLStreamReader xml) throws IOException, XMLStreamException {
    final FileBackedOutputStream xmlBuf = createBuffer();
    XMLEventReader xmlEventReader = null;
    XMLEventWriter xmlEventWriter = null;
    try {
      xmlEventReader = xmlInputFactory.createXMLEventReader(xml);
      xmlEventWriter = xmlOutputFactory.createXMLEventWriter(new OutputStreamWriter(xmlBuf, Text.CHARSET));
      xmlEventWriter.add(xmlEventReader);
    } finally {
      XML.closeQuietly(xmlEventWriter);
      XML.closeQuietly(xmlEventReader);
      Closeables.close(xmlBuf, false);
    }

    Reader xmlBufReader = null;
    try {
      xmlBufReader = new InputStreamReader(xmlBuf.getSupplier().getInput(), Text.CHARSET);
      return write(create(layer, Text.Type.XML), xmlBufReader);
    } finally {
      Closeables.close(xmlBufReader, false);
    }
  }

  @Override
  public Text create(Annotation layer, Reader content) throws IOException {
    return write(create(layer, Text.Type.TXT), content);
  }

  public Iterable<Annotation> create(Annotation... annotations) {
    return create(Arrays.asList(annotations));
  }

  public void delete(Annotation... annotations) {
    delete(Arrays.asList(annotations));
  }

  @Override
  public void read(Text text, final XMLStreamWriter xml) throws IOException, XMLStreamException {
    try {
      Preconditions.checkArgument(text.getType() == Text.Type.XML);
      read(text, new TextConsumer() {
        @Override
        public void read(Reader content, long contentLength) throws IOException {
          XMLEventReader xmlReader = null;
          XMLEventWriter xmlWriter = null;
          try {
            xmlReader = xmlInputFactory.createXMLEventReader(content);
            xmlWriter = xmlOutputFactory.createXMLEventWriter(xml);
            xmlWriter.add(xmlReader);
          } catch (XMLStreamException e) {
            throw Throwables.propagate(e);
          } finally {
            XML.closeQuietly(xmlWriter);
            XML.closeQuietly(xmlReader);
          }
        }
      });
    } catch (IOException e) {
      throw e;
    } catch (Throwable t) {
      Throwables.propagateIfInstanceOf(Throwables.getRootCause(t), XMLStreamException.class);
      throw Throwables.propagate(t);
    }
  }

  @Override
  public String read(Text text, Range range) throws IOException {
    return getOnlyElement(read(text, Sets.newTreeSet(singleton(range))).values());
  }

  public void read(final Text text, final Criterion criterion, final TextListener listener) throws IOException {
    read(text, criterion, defaultPageSize, listener);
  }

  public void read(final Text text, final Criterion criterion, final int pageSize, final TextListener listener) throws IOException {
    read(text, new TextConsumer() {

      public void read(Reader content, long contentLength) throws IOException {
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
            final Range pageRange = new Range(offset, pageEnd);
            final Iterable<Annotation> pageAnnotations = find(and(criterion, text(text), rangeOverlap(pageRange)));
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

            final Set<Annotation> emptyEvents = Sets.newHashSet(Sets.filter(endEvents, EMPTY));
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

  @Override
  public Iterable<Annotation> find(Criterion criterion) {
    final SortedSet<Annotation> result = Sets.newTreeSet();
    scroll(criterion, new AnnotationConsumer() {
      @Override
      public void consume(Annotation annotation) {
        result.add(annotation);
      }
    });
    return result;
  }

  public SortedSet<Name> names(Text text) {
    final SortedSet<Name> names = getNames(text);

    if (names.isEmpty() && text.getType() == Text.Type.XML) {
      try {
        read(text, new TextConsumer() {
          public void read(Reader content, long contentLength) throws IOException {
            if (contentLength == 0) {
              return;
            }
            XMLStreamReader xml = null;
            try {
              xml = xmlInputFactory.createXMLStreamReader(content);
              while (xml.hasNext()) {
                switch(xml.next()) {
                  case XMLStreamReader.START_ELEMENT:
                    names.add(new SimpleName(xml.getName()));
                }
              }
            } catch (XMLStreamException e) {
              throw Throwables.propagate(e);
            } finally {
              XML.closeQuietly(xml);
            }
          }
        });
      } catch (IOException e) {
        throw Throwables.propagate(e);
      }
    }

    return names;
  }

  @Override
  public void transform(Criterion criterion, final Text to, final Function<Annotation, Annotation> transform) {
    final List<Annotation> batch = Lists.newArrayListWithExpectedSize(batchSize);
    scroll(criterion, new AnnotationConsumer() {
      @Override
      public void consume(Annotation annotation) {
        batch.add(annotation);
        if ((batch.size() % batchSize) == 0) {
          transform(batch, to, transform);
          batch.clear();
        }
      }
    });
    if (!batch.isEmpty()) {
      transform(batch, to, transform);
    }
  }

  @Override
  public Iterable<Annotation> transform(Iterable<Annotation> annotations, Text to, Function<Annotation, Annotation> transform) {
    return create(Lists.newArrayList(Iterables.transform(annotations, compose(transform, adopt(to)))));
  }

  protected FileBackedOutputStream createBuffer() {
    return new FileBackedOutputStream(memoryBufferThreshold, true);
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

  protected abstract SortedSet<Name> getNames(Text text);

  public static class RangeFilteringReader extends FilterReader {

    private final Range range;
    private int offset = 0;

    public RangeFilteringReader(Reader in, Range range) {
      super(in);
      this.range = range;
    }

    @Override
    public int read() throws IOException {
      while (offset < range.getStart()) {
        final int read = doRead();
        if (read < 0) {
          return read;
        }
      }
      if (offset >= range.getEnd()) {
        return -1;
      }

      return doRead();
    }

    protected int doRead() throws IOException {
      final int read = super.read();
      if (read >= 0) {
        ++offset;
      }
      return read;
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
      int read = 0;
      int last;
      while ((read < len) && ((last = read()) >= 0)) {
        cbuf[off + read++] = (char) last;
      }
      return ((len > 0 && read == 0) ? -1 : read);
    }
  }

  public static class CountingWriter extends FilterWriter {

    public long length = 0;

    public CountingWriter(Writer out) {
      super(out);
    }

    @Override
    public void write(int c) throws IOException {
      super.write(c);
      length++;
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
      super.write(cbuf, off, len);
      length += len;
    }
    @Override
    public void write(String str, int off, int len) throws IOException {
      super.write(str, off, len);
      length += len;
    }

  }

  private static final Predicate<Annotation> EMPTY = new Predicate<Annotation>() {
    public boolean apply(Annotation input) {
      return input.getRange().length() == 0;
    }
  };
}
