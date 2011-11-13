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
import eu.interedition.text.Range;
import eu.interedition.text.Text;
import eu.interedition.text.TextConsumer;
import eu.interedition.text.TextRepository;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.nio.CharBuffer;
import java.nio.charset.CharsetEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import static com.google.common.collect.Iterables.getOnlyElement;
import static java.util.Collections.singleton;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public abstract class AbstractTextRepository implements TextRepository {
  protected TransformerFactory transformerFactory = TransformerFactory.newInstance();

  public Text create(Source xml) throws IOException, TransformerException {
    final File xmlSource = File.createTempFile(getClass().getName(), ".xml");
    Reader xmlSourceReader = null;
    try {
      createTransformer().transform(xml, new StreamResult(xmlSource));

      xmlSourceReader = new InputStreamReader(new FileInputStream(xmlSource), Text.CHARSET);
      return write(create(Text.Type.XML), xmlSourceReader);
    } finally {
      Closeables.close(xmlSourceReader, false);
      xmlSource.delete();
    }
  }

  @Override
  public Text create(Reader content) throws IOException {
    return write(create(Text.Type.TXT), content);
  }

  @Override
  public void read(Text text, final Result xml) throws IOException, TransformerException {
    try {
      Preconditions.checkArgument(text.getType() == Text.Type.XML);
      read(text, new TextConsumer() {
        @Override
        public void read(Reader content, long contentLength) throws IOException {
          try {
            createTransformer().transform(new StreamSource(content), xml);
          } catch (TransformerException e) {
            throw Throwables.propagate(e);
          }
        }
      });
    } catch (IOException e) {
      throw e;
    } catch (Throwable t) {
      Throwables.propagateIfInstanceOf(Throwables.getRootCause(t), TransformerException.class);
      throw Throwables.propagate(t);
    }
  }

  @Override
  public String read(Text text, Range range) throws IOException {
    return getOnlyElement(bulkRead(text, Sets.newTreeSet(singleton(range))).values());
  }

  @Override
  public Text concat(Text... texts) throws IOException {
    return concat(Arrays.asList(texts));
  }

  @Override
  public Text duplicate(Text text) throws IOException {
    return concat(singleton(text));
  }

  protected Transformer createTransformer() throws TransformerException {
    final Transformer transformer = transformerFactory.newTransformer();
    transformer.setOutputProperty(OutputKeys.METHOD, "xml");
    transformer.setOutputProperty(OutputKeys.ENCODING, Text.CHARSET.name());
    transformer.setOutputProperty(OutputKeys.INDENT, "no");
    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
    return transformer;
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
