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
import com.google.common.io.CharStreams;
import com.google.common.io.Closeables;
import com.google.common.io.FileBackedOutputStream;
import com.google.common.io.Files;
import eu.interedition.text.Range;
import eu.interedition.text.Text;
import eu.interedition.text.TextConsumer;
import eu.interedition.text.util.AbstractTextRepository;
import eu.interedition.text.util.SQL;
import eu.interedition.text.util.TextDigestingFilterReader;
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
import java.sql.Clob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static eu.interedition.text.util.TextDigestingFilterReader.NULL_DIGEST;

public class RelationalTextRepository extends AbstractTextRepository implements InitializingBean {

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
    textData.put("content_digest", NULL_DIGEST);

    textInsert.execute(textData);

    return new RelationalText(type, 0, NULL_DIGEST, id);
  }

  public Text write(Text text, Reader content) throws IOException {
    final FileBackedOutputStream buf = createBuffer();
    CountingWriter tempWriter = null;
    try {
      CharStreams.copy(content, tempWriter = new CountingWriter(new OutputStreamWriter(buf, Text.CHARSET)));
    } finally {
      Closeables.close(tempWriter, false);
    }

    Reader bufReader = null;
    try {
      return write(text, bufReader = new InputStreamReader(buf.getSupplier().getInput(), Text.CHARSET), tempWriter.length);
    } finally {
      Closeables.close(bufReader, false);
    }
  }

  public void delete(Text text) {
    jt.update("delete from text_content where id = ?", ((RelationalText) text).getId());
  }

  public void read(Text text, final TextConsumer consumer) throws IOException {
    read(new ReaderCallback<Void>(text) {

      @Override
      protected Void read(Clob content) throws SQLException, IOException {
        Reader contentReader = null;
        try {
          consumer.read(contentReader = content.getCharacterStream(), content.length());
        } catch (IOException e) {
          Throwables.propagate(e);
        } finally {
          Closeables.close(contentReader, false);
        }
        return null;
      }
    });
  }

  public void read(Text text, final Range range, final TextConsumer consumer) throws IOException {
    read(new ReaderCallback<Void>(text) {

      @Override
      protected Void read(Clob content) throws SQLException, IOException {
        Reader contentReader = null;
        try {
          consumer.read(contentReader = new RangeFilteringReader(content.getCharacterStream(), range), range.length());
        } catch (IOException e) {
          Throwables.propagate(e);
        } finally {
          Closeables.close(contentReader, false);
        }
        return null;
      }
    });
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

  public Text write(final Text text, final Reader contents, final long contentLength) throws IOException {
    final long id = ((RelationalText) text).getId();
    final TextDigestingFilterReader digestingFilterReader = new TextDigestingFilterReader(new BufferedReader(contents));
    jt.getJdbcOperations().execute("update text_content set content = ?, content_length = ? where id = ?", new PreparedStatementCallback<Void>() {
      public Void doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException {
        ps.setCharacterStream(1, digestingFilterReader, contentLength);
        ps.setLong(2, contentLength);
        ps.setLong(3, id);
        ps.executeUpdate();
        return null;
      }
    });
    final byte[] digest = digestingFilterReader.digest();
    jt.update("update text_content set content_digest = ? where id = ?", digest, id);
    return new RelationalText(text.getType(), contentLength, digest, id);
  }

  @Override
  public Text concat(Iterable<Text> texts) throws IOException {
    final FileBackedOutputStream buf = createBuffer();
    final OutputStreamWriter bufWriter = new OutputStreamWriter(buf, Text.CHARSET);
    try {
      for (Text text : texts) {
        read(new ReaderCallback<Void>(text) {
          @Override
          protected Void read(Clob content) throws SQLException, IOException {
            Reader reader = null;
            try {
              CharStreams.copy(reader = content.getCharacterStream(), bufWriter);
            } finally {
              Closeables.close(reader, false);
            }
            return null;
          }
        });
      }
    } finally {
       Closeables.close(bufWriter, false);
    }

    Reader reader = null;
    try {
      return create(reader = new InputStreamReader(buf.getSupplier().getInput(), Text.CHARSET));
    } finally {
      Closeables.closeQuietly(reader);
      buf.reset();
    }
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
    for (Iterator<Long> it = ids.iterator(); it.hasNext(); ) {
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
    return new RelationalText(Text.Type.values()[rs.getInt(prefix + "_type")],//
            rs.getLong(prefix + "_content_length"),//
            rs.getBytes(prefix + "_content_digest"),//
            rs.getLong(prefix + "_id"));
  }

  private abstract class ReaderCallback<T> {
    private final RelationalText text;

    private ReaderCallback(Text text) {
      this.text = (RelationalText) text;
    }

    protected abstract T read(Clob content) throws SQLException, IOException;
  }
}
