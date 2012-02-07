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
import com.google.common.io.Closeables;
import com.google.common.io.FileBackedOutputStream;
import eu.interedition.text.Annotation;
import eu.interedition.text.Range;
import eu.interedition.text.Text;
import eu.interedition.text.TextListener;
import eu.interedition.text.TextRepository;
import eu.interedition.text.query.Criterion;
import eu.interedition.text.xml.XML;
import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.stax2.XMLOutputFactory2;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import java.io.FilterReader;
import java.io.FilterWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Functions.compose;
import static eu.interedition.text.transform.AnnotationTransformers.adopt;

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
    Preconditions.checkArgument(text.getType() == Text.Type.XML);
    Reader xmlStream = null;
    XMLEventReader xmlReader = null;
    XMLEventWriter xmlWriter = null;
    try {
      xmlReader = xmlInputFactory.createXMLEventReader(xmlStream = read(text).getInput());
      xmlWriter = xmlOutputFactory.createXMLEventWriter(xml);
      xmlWriter.add(xmlReader);
    } catch (XMLStreamException e) {
      throw Throwables.propagate(e);
    } finally {
      XML.closeQuietly(xmlWriter);
      XML.closeQuietly(xmlReader);
      Closeables.close(xmlStream, false);
    }
  }

  public void read(final Text text, final Criterion criterion, final TextListener listener) throws IOException {
    read(text, criterion, defaultPageSize, listener);
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

  protected static final Predicate<Annotation> EMPTY = new Predicate<Annotation>() {
    public boolean apply(Annotation input) {
      return input.getRange().length() == 0;
    }
  };
}
