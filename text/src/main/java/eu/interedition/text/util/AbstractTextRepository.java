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

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.Sets;
import com.google.common.io.Closeables;
import com.google.common.io.FileBackedOutputStream;
import eu.interedition.text.Annotation;
import eu.interedition.text.Range;
import eu.interedition.text.Text;
import eu.interedition.text.TextConsumer;
import eu.interedition.text.TextRepository;
import eu.interedition.text.xml.XML;
import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.stax2.XMLOutputFactory2;

import javax.xml.stream.*;
import java.io.*;
import java.util.Arrays;

import static com.google.common.collect.Iterables.getOnlyElement;
import static java.util.Collections.singleton;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public abstract class AbstractTextRepository implements TextRepository {
  protected final XMLInputFactory2 xmlInputFactory = XML.createXMLInputFactory();
  protected final XMLOutputFactory2 xmlOutputFactory = XML.createXMLOutputFactory();

  private int memoryBufferThreshold = 1001024;

  public void setMemoryBufferThreshold(int memoryBufferThreshold) {
    this.memoryBufferThreshold = memoryBufferThreshold;
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
    return getOnlyElement(bulkRead(text, Sets.newTreeSet(singleton(range))).values());
  }

  protected FileBackedOutputStream createBuffer() {
    return new FileBackedOutputStream(memoryBufferThreshold, true);
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
}
