package eu.interedition.text.rdbms;

import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.CharStreams;
import com.google.common.io.Closeables;
import com.google.common.io.Files;
import eu.interedition.text.Range;
import eu.interedition.text.Text;
import eu.interedition.text.TextRepository;
import eu.interedition.text.xml.XMLParser;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import javax.sql.DataSource;
import java.io.*;
import java.sql.Clob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;

import static com.google.common.collect.Iterables.getOnlyElement;
import static java.util.Collections.singleton;

public class RelationalTextRepository implements TextRepository {

  private SimpleJdbcTemplate jt;
  private SimpleJdbcInsert textInsert;

  public void setDataSource(DataSource dataSource) {
    this.jt = (dataSource == null ? null : new SimpleJdbcTemplate(dataSource));
    this.textInsert = (jt == null ? null : new SimpleJdbcInsert(dataSource).withTableName("text_content").usingGeneratedKeyColumns("id"));
  }

  public Text create(Text.Type type) {
    final Date created = new Date();

    final Map<String, Object> textData = Maps.newHashMap();
    textData.put("created", created);
    textData.put("type", type.ordinal());
    textData.put("content", "");

    final RelationalText relationalText = new RelationalText();
    relationalText.setCreated(created);
    relationalText.setType(type);
    relationalText.setId(textInsert.executeAndReturnKey(textData).intValue());

    return relationalText;
  }

  public Text create(Reader content) throws IOException {
    final Text text = create(Text.Type.PLAIN);
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

  public String read(Text text, Range range) throws IOException {
    return getOnlyElement(bulkRead(text, Sets.newTreeSet(singleton(range))).values());
  }

  public int length(Text text) throws IOException {
    return read(new ReaderCallback<Integer>(text) {

      @Override
      protected Integer read(Clob content) throws SQLException, IOException {
        return (int) content.length();
      }
    });
  }

  public SortedMap<Range, String> bulkRead(Text text, final SortedSet<Range> ranges) throws IOException {
    final SortedMap<Range, String> results = Maps.newTreeMap();
    read(new ReaderCallback<Void>(text) {

      @Override
      protected Void read(Clob content) throws SQLException, IOException {
        for (Range range : ranges) {
          results.put(range, content.getSubString(range.getStart() + 1, range.length()));
        }
        return null;
      }
    });
    return results;
  }

  public void write(final Text text, final Reader contents, final int contentLength) throws IOException {
    jt.getJdbcOperations().execute("update text_content set content = ? where id = ?", new PreparedStatementCallback<Void>() {
      public Void doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException {
        ps.setCharacterStream(1, new BufferedReader(contents), contentLength);
        ps.setInt(2, ((RelationalText) text).getId());
        ps.executeUpdate();
        return null;
      }
    });
  }


  private <T> T read(final ReaderCallback<T> callback) {
    return DataAccessUtils.requiredUniqueResult(jt.query("select content from text_content where id = ?",
            new RowMapper<T>() {

              public T mapRow(ResultSet rs, int rowNum) throws SQLException {
                try {
                  return (callback.result = callback.read(rs.getClob(1)));
                } catch (IOException e) {
                  throw new SQLException(e);
                }
              }
            }, callback.text.getId()));
  }

  public Text load(int id) {
    return DataAccessUtils.requiredUniqueResult(jt.query("select " + select("t") + " from text_content t where t.id = ?", new RowMapper<Text>() {
      public Text mapRow(ResultSet rs, int rowNum) throws SQLException {
        return mapText(rs, "t");
      }
    }, id));
  }

  static String select(String tableName) {
    return Util.select(tableName, "id", "created", "type");
  }

  static RelationalText mapText(ResultSet rs, String prefix) throws SQLException {
    final RelationalText relationalText = new RelationalText();
    relationalText.setId(rs.getInt(prefix + "_id"));
    relationalText.setCreated(rs.getDate(prefix + "_created"));
    relationalText.setType(Text.Type.values()[rs.getInt(prefix + "_type")]);
    return relationalText;
  }

  private abstract class ReaderCallback<T> {
    private final RelationalText text;
    private T result;

    private ReaderCallback(Text text) {
      this.text = (RelationalText) text;
    }

    protected abstract T read(Clob content) throws SQLException, IOException;
  }

  private static class CountingWriter extends FilterWriter {

    private int length = 0;

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
}
