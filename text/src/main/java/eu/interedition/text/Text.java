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
package eu.interedition.text;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import com.google.common.io.CharStreams;
import com.google.common.io.Closeables;
import com.google.common.io.FileBackedOutputStream;
import com.google.common.io.InputSupplier;
import eu.interedition.text.util.TextDigestingFilterReader;
import eu.interedition.text.xml.XML;
import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.stax2.XMLOutputFactory2;
import org.hibernate.Hibernate;
import org.hibernate.Session;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import java.io.BufferedReader;
import java.io.FilterReader;
import java.io.FilterWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.SortedMap;
import java.util.SortedSet;

import static eu.interedition.text.util.TextDigestingFilterReader.NULL_DIGEST;

@Entity
@Table(name = "interedition_text")
public class Text {
  public static final Charset CHARSET = Charset.forName("UTF-8");

  private static int MEMORY_BUFFER_THRESHOLD = 1001024;

  private static final XMLInputFactory2 XML_INPUT_FACTORY = XML.createXMLInputFactory();
  private static final XMLOutputFactory2 XML_OUTPUT_FACTORY = XML.createXMLOutputFactory();

  public enum Type {
    TXT, XML
  }

  protected long id;
  protected Annotation layer;
  protected Type type;
  protected long length;
  protected byte[] digest;
  protected Clob content;

  @Id
  @GeneratedValue
  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  @ManyToOne
  @JoinColumn(name = "layer_id")
  public Annotation getLayer() {
    return layer;
  }

  public void setLayer(Annotation layer) {
    this.layer = layer;
  }

  @Enumerated
  @Column(name = "text_type", nullable = false)
  public Type getType() {
    return type;
  }

  public void setType(Type type) {
    this.type = type;
  }

  @Column(name = "content_length", nullable = false)
  public long getLength() {
    return length;
  }

  public void setLength(long length) {
    this.length = length;
  }

  @Column(name = "content_digest", length = 64)
  public byte[] getDigest() {
    return digest;
  }

  public void setDigest(byte[] digest) {
    this.digest = digest;
  }

  @Lob
  public Clob getContent() {
    return content;
  }

  public void setContent(Clob content) {
    this.content = content;
  }

  public InputSupplier<Reader> read() {
    return new InputSupplier<Reader>() {
      @Override
      public Reader getInput() throws IOException {
        try {
          return content.getCharacterStream();
        } catch (SQLException e) {
          throw Throwables.propagate(e);
        }
      }
    };
  }

  public InputSupplier<Reader> read(final TextRange range) {
    return new InputSupplier<Reader>() {
      @Override
      public Reader getInput() throws IOException {
        try {
          return new RangeFilteringReader(content.getCharacterStream(), range);
        } catch (SQLException e) {
          throw Throwables.propagate(e);
        }
      }
    };
  }

  public SortedMap<TextRange, String> read(final SortedSet<TextRange> ranges) throws IOException {
    try {
      final SortedMap<TextRange, String> results = Maps.newTreeMap();
      for (TextRange range : ranges) {
        results.put(range, content.getSubString(range.getStart() + 1, (int) range.length()));
      }
      return results;
    } catch (SQLException e) {
      throw Throwables.propagate(e);
    }
  }

  public void read(final XMLStreamWriter xml) throws IOException, XMLStreamException {
    Preconditions.checkArgument(getType() == Text.Type.XML);
    Reader xmlStream = null;
    XMLEventReader xmlReader = null;
    XMLEventWriter xmlWriter = null;
    try {
      xmlReader = XML_INPUT_FACTORY.createXMLEventReader(xmlStream = read().getInput());
      xmlWriter = XML_OUTPUT_FACTORY.createXMLEventWriter(xml);
      xmlWriter.add(xmlReader);
    } catch (XMLStreamException e) {
      throw Throwables.propagate(e);
    } finally {
      XML.closeQuietly(xmlWriter);
      XML.closeQuietly(xmlReader);
      Closeables.close(xmlStream, false);
    }
  }


  public Text write(Session session, Reader content) throws IOException {
    final FileBackedOutputStream buf = createBuffer();
    CountingWriter tempWriter = null;
    try {
      CharStreams.copy(content, tempWriter = new CountingWriter(new OutputStreamWriter(buf, Text.CHARSET)));
    } finally {
      Closeables.close(tempWriter, false);
    }

    Reader bufReader = null;
    try {
      return write(session, bufReader = new InputStreamReader(buf.getSupplier().getInput(), Text.CHARSET), tempWriter.length);
    } finally {
      Closeables.close(bufReader, false);
    }
  }

  public Text write(Session session, Reader contents, long contentLength) throws IOException {
    Text text = (Text) session.merge(this);

    final TextDigestingFilterReader digestingFilterReader = new TextDigestingFilterReader(new BufferedReader(contents));
    text.setLength(contentLength);
    text.setContent(Hibernate.getLobCreator(session).createClob(digestingFilterReader, contentLength));

    session.flush();
    session.refresh(text);

    text.setDigest(digestingFilterReader.digest());

    return text;
  }


  @Override
  public String toString() {
    return Objects.toStringHelper(this).addValue(layer).add("type", type).add("length", length).add("id", Long.toString(id)).toString();
  }

  @Override
  public boolean equals(Object obj) {
    if (id != 0 && obj != null && obj instanceof Text) {
      return id == ((Text) obj).id;
    }
    return super.equals(obj);
  }

  @Override
  public int hashCode() {
    return (id == 0 ? super.hashCode() : Objects.hashCode(id));
  }

  public static Text create(Session session, Annotation layer, Text.Type type) {
    Text text = new Text();
    text.setLayer(layer);
    text.setType(type);
    text.setContent(Hibernate.getLobCreator(session).createClob(""));
    text.setLength(0);

    text = (Text) session.merge(text);
    session.flush();
    session.refresh(text);

    text.setDigest(NULL_DIGEST);
    return text;
  }

  public static Text create(Session session, Annotation layer, XMLStreamReader xml) throws IOException, XMLStreamException {
    final FileBackedOutputStream xmlBuf = createBuffer();
    XMLEventReader xmlEventReader = null;
    XMLEventWriter xmlEventWriter = null;
    try {
      xmlEventReader = XML_INPUT_FACTORY.createXMLEventReader(xml);
      xmlEventWriter = XML_OUTPUT_FACTORY.createXMLEventWriter(new OutputStreamWriter(xmlBuf, Text.CHARSET));
      xmlEventWriter.add(xmlEventReader);
    } finally {
      XML.closeQuietly(xmlEventWriter);
      XML.closeQuietly(xmlEventReader);
      Closeables.close(xmlBuf, false);
    }

    Reader xmlBufReader = null;
    try {
      xmlBufReader = new InputStreamReader(xmlBuf.getSupplier().getInput(), Text.CHARSET);
      return create(session, layer, Text.Type.XML).write(session, xmlBufReader);
    } finally {
      Closeables.close(xmlBufReader, false);
    }
  }

  public static Text create(Session session, Annotation layer, Reader content) throws IOException {
    return create(session, layer, Text.Type.TXT).write(session, content);
  }

  private static FileBackedOutputStream createBuffer() {
    return new FileBackedOutputStream(MEMORY_BUFFER_THRESHOLD, true);
  }

  private static class CountingWriter extends FilterWriter {

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
  private static class RangeFilteringReader extends FilterReader {

    private final TextRange range;
    private int offset = 0;

    public RangeFilteringReader(Reader in, TextRange range) {
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
