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
package eu.interedition.text.rdbms;

import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.CharStreams;
import com.google.common.io.Closeables;
import com.google.common.io.Files;
import eu.interedition.text.Range;
import eu.interedition.text.Text;
import eu.interedition.text.util.AbstractTextRepository;
import eu.interedition.text.util.SQL;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.jdbc.support.incrementer.DataFieldMaxValueIncrementer;

import javax.sql.DataSource;
import java.io.*;
import java.nio.CharBuffer;
import java.nio.charset.CharsetEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Clob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static com.google.common.collect.Iterables.getOnlyElement;
import static java.util.Collections.singleton;

public class RelationalTextRepository extends AbstractTextRepository implements InitializingBean {

  private static final String NULL_CONTENT_DIGEST = DigestUtils.sha512Hex("");

  private DataSource dataSource;
  private DataFieldMaxValueIncrementerFactory incrementerFactory;

  private SimpleJdbcTemplate jt;
  private SimpleJdbcInsert textInsert;
  private DataFieldMaxValueIncrementer textIdIncrementer;

  @Required
  public void setDataSource(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  @Required
  public void setIncrementerFactory(DataFieldMaxValueIncrementerFactory incrementerFactory) {
    this.incrementerFactory = incrementerFactory;
  }

  public void afterPropertiesSet() throws Exception {
    this.jt = (dataSource == null ? null : new SimpleJdbcTemplate(dataSource));
    this.textInsert = (jt == null ? null : new SimpleJdbcInsert(dataSource).withTableName("text_content"));
    this.textIdIncrementer = incrementerFactory.create("text_content");
  }

  public Text create(Text.Type type) {
    final long id = textIdIncrementer.nextLongValue();

    final Map<String, Object> textData = Maps.newHashMap();
    textData.put("id", id);
    textData.put("type", type.ordinal());
    textData.put("content", "");
    textData.put("content_length", 0);
    textData.put("content_digest", NULL_CONTENT_DIGEST);

    textInsert.execute(textData);

    final RelationalText rt = new RelationalText();
    rt.setId(id);
    rt.setType(type);
    rt.setLength(0);

    return rt;
  }

  public Text create(Reader content) throws IOException {
    final Text text = create(Text.Type.TXT);
    write(text, content);
    return text;
  }

  public void write(Text text, Reader content) throws IOException {
    final File tempFile = File.createTempFile(getClass().toString(), ".txt");
    try {
      CountingWriter tempWriter = null;
      try {
        tempWriter = new CountingWriter(new OutputStreamWriter(new FileOutputStream(tempFile), Text.CHARSET));
        CharStreams.copy(content, tempWriter);
      } finally {
        Closeables.close(tempWriter, false);
      }

      BufferedReader textReader = null;
      try {
        textReader = Files.newReader(tempFile, Text.CHARSET);
        write(text, textReader, tempWriter.length);
      } finally {
        Closeables.close(textReader, false);
      }
    } finally {
      tempFile.delete();
    }
  }

  public void delete(Text text) {
    jt.update("delete from text_content where id = ?", ((RelationalText) text).getId());
  }

  public void read(Text text, final TextReader reader) throws IOException {
    read(new ReaderCallback<Void>(text) {

      @Override
      protected Void read(Clob content) throws SQLException, IOException {
        Reader contentReader = null;
        try {
          reader.read(contentReader = content.getCharacterStream(), (int) content.length());
        } catch (IOException e) {
          Throwables.propagate(e);
        } finally {
          Closeables.close(contentReader, false);
        }
        return null;
      }
    });
  }

  public void read(Text text, final Range range, final TextReader reader) throws IOException {
    read(new ReaderCallback<Void>(text) {

      @Override
      protected Void read(Clob content) throws SQLException, IOException {
        Reader contentReader = null;
        try {
          reader.read(contentReader = content.getCharacterStream(range.getStart() + 1, range.length()), range.length());
        } catch (IOException e) {
          Throwables.propagate(e);
        } finally {
          Closeables.close(contentReader, false);
        }
        return null;
      }
    });
  }

  public String read(Text text, Range range) throws IOException {
    return getOnlyElement(bulkRead(text, Sets.newTreeSet(singleton(range))).values());
  }

  public SortedMap<Range, String> bulkRead(Text text, final SortedSet<Range> ranges) throws IOException {
    final SortedMap<Range, String> results = Maps.newTreeMap();
    read(new ReaderCallback<Void>(text) {

      @Override
      protected Void read(Clob content) throws SQLException, IOException {
        for (Range range : ranges) {
          results.put(range, content.getSubString(range.getStart() + 1, (int) range.length()));
        }
        return null;
      }
    });
    return results;
  }

  public void write(final Text text, final Reader contents, final long contentLength) throws IOException {
    final RelationalText rt = (RelationalText) text;
    final DigestingFilterReader digestingFilterReader = new DigestingFilterReader(new BufferedReader(contents));
    jt.getJdbcOperations().execute("update text_content set content = ?, content_length = ? where id = ?", new PreparedStatementCallback<Void>() {
      public Void doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException {
        ps.setCharacterStream(1, digestingFilterReader, contentLength);
        ps.setLong(2, contentLength);
        ps.setLong(3, rt.getId());
        ps.executeUpdate();
        return null;
      }
    });
    rt.setLength(contentLength);
    rt.setDigest(digestingFilterReader.digest());
    jt.update("update text_content set content_digest = ? where id = ?", rt.getDigest(), rt.getId());
  }


  private <T> T read(final ReaderCallback<T> callback) {
    return DataAccessUtils.requiredUniqueResult(jt.query("select content from text_content where id = ?",
            new RowMapper<T>() {

              public T mapRow(ResultSet rs, int rowNum) throws SQLException {
                try {
                  return callback.read(rs.getClob(1));
                } catch (IOException e) {
                  throw new SQLException(e);
                }
              }
            }, callback.text.getId()));
  }

  public List<Text> load(Iterable<Long> ids) {
    if (Iterables.isEmpty(ids)) {
      return Collections.emptyList();
    }

    final List<Long> idList = Lists.newArrayList(ids);
    final StringBuilder sql = new StringBuilder("select ");
    sql.append(selectTextFrom("t"));
    sql.append(" from text_content t where t.id in (");
    for(Iterator<Long> it = ids.iterator(); it.hasNext(); ) {
      it.next();
      sql.append("?").append(it.hasNext() ? ", " : "");
    }
    sql.append(")");

    return jt.query(sql.toString(), new RowMapper<Text>() {
      @Override
      public Text mapRow(ResultSet rs, int rowNum) throws SQLException {
        return mapTextFrom(rs, "t");
      }
    }, idList.toArray(new Object[idList.size()]));
  }

  public Text load(long id) {
    return DataAccessUtils.requiredUniqueResult(load(Collections.singleton(id)));
  }

  public static String selectTextFrom(String tableName) {
    return SQL.select(tableName, "id", "type", "content_length", "content_digest");
  }

  public static RelationalText mapTextFrom(ResultSet rs, String prefix) throws SQLException {
    final RelationalText rt = new RelationalText();
    rt.setId(rs.getInt(prefix + "_id"));
    rt.setType(Text.Type.values()[rs.getInt(prefix + "_type")]);
    rt.setLength(rs.getLong(prefix + "_content_length"));
    rt.setDigest(rs.getString(prefix + "_content_digest"));
    return rt;
  }

  private abstract class ReaderCallback<T> {
    private final RelationalText text;

    private ReaderCallback(Text text) {
      this.text = (RelationalText) text;
    }

    protected abstract T read(Clob content) throws SQLException, IOException;
  }

  private static class CountingWriter extends FilterWriter {

    private long length = 0;

    private CountingWriter(Writer out) {
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

  private static class DigestingFilterReader extends FilterReader {

    private MessageDigest digest;
    private String result;
    private CharsetEncoder encoder;

    private DigestingFilterReader(Reader in) {
      super(in);
      try {
        this.digest = MessageDigest.getInstance("SHA-512");
        this.encoder = Text.CHARSET.newEncoder();
      } catch (NoSuchAlgorithmException e) {
        throw Throwables.propagate(e);
      }
    }

    @Override
    public int read() throws IOException {
      final int read = super.read();
      if (read >= 0) {
        digest.update(encoder.encode(CharBuffer.wrap(new char[]{(char) read})));
      }
      return read;
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
      final int read = super.read(cbuf, off, len);
      if (read >= 0) {
        digest.update(encoder.encode(CharBuffer.wrap(cbuf, off, len)));
      }
      return read;
    }

    @Override
    public void reset() throws IOException {
      digest.reset();
      result = null;
      super.reset();
    }

    private String digest() {
      if (result == null) {
        result = Hex.encodeHexString(digest.digest());
      }
      return result;
    }
  }
}
