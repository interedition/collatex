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

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.CharStreams;
import com.google.common.io.Closeables;
import com.google.common.io.FileBackedOutputStream;
import eu.interedition.text.Annotation;
import eu.interedition.text.AnnotationConsumer;
import eu.interedition.text.Name;
import eu.interedition.text.Range;
import eu.interedition.text.Text;
import eu.interedition.text.TextConsumer;
import eu.interedition.text.mem.SimpleAnnotation;
import eu.interedition.text.query.Criterion;
import eu.interedition.text.util.AbstractTextRepository;
import eu.interedition.text.util.Annotations;
import eu.interedition.text.util.SQL;
import eu.interedition.text.util.TextDigestingFilterReader;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.incrementer.DataFieldMaxValueIncrementer;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.sql.Clob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;

import static eu.interedition.text.rdbms.RelationalNameRegistry.mapNameFrom;
import static eu.interedition.text.rdbms.RelationalNameRegistry.selectNameFrom;
import static eu.interedition.text.util.TextDigestingFilterReader.NULL_DIGEST;

public class RelationalTextRepository extends AbstractTextRepository implements InitializingBean {

  private DataSource dataSource;
  private JdbcTemplate jt;

  private RelationalDatabaseKeyFactory keyFactory;
  private DataFieldMaxValueIncrementer textIdIncrementer;
  private DataFieldMaxValueIncrementer annotationIdIncrementer;

  private RelationalNameRegistry nameRegistry;

  private SimpleJdbcInsert textInsert;
  private SimpleJdbcInsert annotationInsert;

  private RelationalQueryCriteriaTranslator queryCriteriaTranslator;

  @Required
  public void setDataSource(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  @Required
  public void setKeyFactory(RelationalDatabaseKeyFactory keyFactory) {
    this.keyFactory = keyFactory;
  }

  @Required
  public void setNameRegistry(RelationalNameRegistry nameRegistry) {
    this.nameRegistry = nameRegistry;
  }

  @Required
  public void setQueryCriteriaTranslator(RelationalQueryCriteriaTranslator queryCriteriaTranslator) {
    this.queryCriteriaTranslator = queryCriteriaTranslator;
  }

  public Text create(Annotation layer, Text.Type type) {
    Preconditions.checkArgument(layer == null || layer instanceof RelationalAnnotation);
            
    final long id = textIdIncrementer.nextLongValue();

    final Map<String, Object> textData = Maps.newHashMap();
    textData.put("id", id);
    textData.put("layer", (layer == null ? null : ((RelationalAnnotation) layer).getId()));
    textData.put("type", type.ordinal());
    textData.put("content", "");
    textData.put("content_length", 0);
    textData.put("content_digest", NULL_DIGEST);

    textInsert.execute(textData);

    return new RelationalText(layer, type, 0, NULL_DIGEST, id);
  }

  public Iterable<Annotation> create(Iterable<Annotation> annotations) {
    final Set<Name> names = Sets.newHashSet(Iterables.transform(annotations, Annotations.NAME));
    final Map<Name, Long> nameIds = Maps.newHashMap();
    for (Name name : nameRegistry.get(names)) {
      nameIds.put(name, ((RelationalName)name).getId());
    }

    final List<Annotation> created = Lists.newArrayList();
    final List<SqlParameterSource> annotationBatch = Lists.newArrayList();
    for (Annotation a : annotations) {
      final long id = annotationIdIncrementer.nextLongValue();
      final Long nameId = nameIds.get(a.getName());
      final Range range = a.getRange();
      final byte[] rawData = SimpleAnnotation.toRawData(a);

      annotationBatch.add(new MapSqlParameterSource()
              .addValue("id", id)
              .addValue("text", ((RelationalText) a.getText()).getId())
              .addValue("name", nameId)
              .addValue("range_start", range.getStart())
              .addValue("range_end", range.getEnd())
              .addValue("json_data", rawData));

      created.add(new RelationalAnnotation(a.getText(), new RelationalName(a.getName(), nameId), range, rawData, id));
    }

    if (!annotationBatch.isEmpty()) {
      annotationInsert.executeBatch(annotationBatch.toArray(new SqlParameterSource[annotationBatch.size()]));
    }
    return created;
  }

  public void delete(Text text) {
    jt.update("delete from text_content where id = ?", ((RelationalText) text).getId());
  }

  public void delete(Criterion criterion) {
    final ArrayList<Object> parameters = new ArrayList<Object>();
    final List<Object[]> batchParameters = Lists.newArrayListWithCapacity(batchSize);

    jt.query(buildAnnotationQuery("select a.id as a_id", parameters, criterion).toString(), new RowMapper<Void>() {
      public Void mapRow(ResultSet rs, int rowNum) throws SQLException {
        batchParameters.add(new Object[]{rs.getInt("a_id")});

        if (rs.isLast() || (batchParameters.size() % batchSize) == 0) {
          jt.batchUpdate("delete from text_annotation where id = ?", batchParameters);
          batchParameters.clear();
        }

        return null;
      }
    }, parameters.toArray(new Object[parameters.size()]));
  }

  public void delete(Iterable<Annotation> annotations) {
    final List<Long> annotationIds = Lists.newArrayList();
    for (Annotation a : annotations) {
      annotationIds.add(((RelationalAnnotation)a).getId());
    }
    if (annotationIds.isEmpty()) {
      return;
    }
    final StringBuilder sql = new StringBuilder("delete from text_annotation where id in (");
    for (Iterator<Long> idIt = annotationIds.iterator(); idIt.hasNext(); ) {
      sql.append("?").append(idIt.hasNext() ? ", " : "");
    }
    sql.append(")");
    jt.update(sql.toString(), annotationIds.toArray(new Object[annotationIds.size()]));
  }


  public Text load(long id) {
    return DataAccessUtils.requiredUniqueResult(load(Collections.singleton(id)));
  }

  public List<Text> load(Iterable<Long> ids) {
    if (Iterables.isEmpty(ids)) {
      return Collections.emptyList();
    }

    final List<Long> idList = Lists.newArrayList(ids);
    final StringBuilder sql = new StringBuilder("select ");
    sql.append(selectTextFrom("t"));
    sql.append(", ").append(selectAnnotationFrom("l"));
    sql.append(", ").append(selectNameFrom("ln"));
    sql.append(" from text_content t");
    sql.append(" left join text_annotation l on t.layer = l.id");
    sql.append(" left join text_qname ln on l.name = ln.id");
    sql.append(" where t.id in (");
    for (Iterator<Long> it = ids.iterator(); it.hasNext(); ) {
      it.next();
      sql.append("?").append(it.hasNext() ? ", " : "");
    }
    sql.append(")");

    return jt.query(sql.toString(), new RowMapper<Text>() {
      private final Map<Long, RelationalName> nameCache = Maps.newHashMap();

      @Override
      public Text mapRow(ResultSet rs, int rowNum) throws SQLException {
        final long layerNameId = rs.getLong("l_id");

        RelationalAnnotation layer = null;
        if (layerNameId != 0) {
          RelationalName name = nameCache.get(layerNameId);
          if (name == null) {
            nameCache.put(layerNameId, name = mapNameFrom(rs, "ln"));
          }
          layer = mapAnnotationFrom(rs, null, name, "l");
        }

        return mapTextFrom(rs, "t", layer);
      }
    }, idList.toArray(new Object[idList.size()]));
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

  public SortedMap<Range, String> read(Text text, final SortedSet<Range> ranges) throws IOException {
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

  public Text write(final Text text, final Reader contents, final long contentLength) throws IOException {
    final long id = ((RelationalText) text).getId();
    final TextDigestingFilterReader digestingFilterReader = new TextDigestingFilterReader(new BufferedReader(contents));
    jt.execute("update text_content set content = ?, content_length = ? where id = ?", new PreparedStatementCallback<Void>() {
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
    return new RelationalText(text.getLayer(), text.getType(), contentLength, digest, id);
  }

  @Override
  public void scroll(Criterion criterion, final AnnotationConsumer consumer) {
    final List<Object> ps = Lists.newArrayList();
    final StringBuilder sql = buildAnnotationQuery(new StringBuilder("select  ")
            .append(selectAnnotationFrom("a")).append(", ")
            .append(selectNameFrom("n")).append(", ")
            .append(selectTextFrom("t")).append(", ")
            .append(selectAnnotationFrom("l")).append(", ")
            .append(selectNameFrom("ln")).toString(), ps, criterion);

    sql.append(" order by a.id, n.id, ln.id");


    jt.query(sql.toString(), new RowMapper<Void>() {
      private final Map<Long, RelationalName> nameCache = Maps.newHashMap();
      private final Map<Long, RelationalText> textCache = Maps.newHashMap();

      public Void mapRow(ResultSet rs, int rowNum) throws SQLException {
        consumer.consume(new RelationalAnnotation(
                text(rs.getLong("t_id"), rs),
                name(rs.getLong("n_id"), rs, "n"),
                new Range(rs.getLong("a_range_start"), rs.getLong("a_range_end")),
                rs.getBytes("a_json_data"),
                rs.getLong("a_id")));

        return null;
      }

      protected RelationalName name(long id, ResultSet rs, String prefix) throws SQLException {
        RelationalName name = nameCache.get(id);
        if (name == null) {
          nameCache.put(id, name = mapNameFrom(rs, prefix));
        }
        return name;
      }

      protected RelationalText text(long id, ResultSet rs) throws SQLException {
        RelationalText text = textCache.get(id);
        if (text == null) {
          final long layerId = rs.getLong("l_id");
          RelationalAnnotation layer = null;
          if (layerId != 0) {
            layer = mapAnnotationFrom(rs, null, name(rs.getLong("ln_id"), rs, "ln"), "l");
          }
          text = mapTextFrom(rs, "t", layer);
          textCache.put(id, text);
        }
        return text;
      }

    }, ps.toArray(new Object[ps.size()]));
  }


  @Override
  protected SortedSet<Name> getNames(Text text) {
    final StringBuilder namesSql = new StringBuilder("select distinct ");
    namesSql.append(selectNameFrom("n"));
    namesSql.append(" from text_qname n join text_annotation a on a.name = n.id where a.text = ?");
    return Sets.newTreeSet(jt.query(namesSql.toString(), new RowMapper<Name>() {

      public Name mapRow(ResultSet rs, int rowNum) throws SQLException {
        return mapNameFrom(rs, "n");
      }
    }, ((RelationalText) text).getId()));
  }

  public void afterPropertiesSet() throws Exception {
    this.jt = (dataSource == null ? null : new JdbcTemplate(dataSource));

    this.textInsert = (jt == null ? null : new SimpleJdbcInsert(dataSource).withTableName("text_content"));
    this.annotationInsert = (jt == null ? null : new SimpleJdbcInsert(dataSource).withTableName("text_annotation"));

    this.textIdIncrementer = keyFactory.create("text_content");
    this.annotationIdIncrementer = keyFactory.create("text_annotation");
  }

  private StringBuilder buildAnnotationQuery(String select, List<Object> ps, Criterion criterion) {
    return queryCriteriaTranslator.where(buildAnnotationFrom(new StringBuilder(select)), criterion, ps);
  }

  private StringBuilder buildAnnotationFrom(StringBuilder sql) {
    sql.append(" from text_annotation a");
    sql.append(" join text_qname n on a.name = n.id");
    sql.append(" join text_content t on a.text = t.id");
    sql.append(" left join text_annotation l on t.layer = l.id");
    sql.append(" left join text_qname ln on l.name = ln.id");
    return sql;
  }

  public static String selectAnnotationFrom(String tableName) {
    return SQL.select(tableName, "id", "range_start", "range_end", "json_data");
  }

  public static String selectTextFrom(String tableName) {
    return SQL.select(tableName, "id", "type", "content_length", "content_digest");
  }

  public static RelationalAnnotation mapAnnotationFrom(ResultSet rs, RelationalText text, RelationalName name, String tableName) throws SQLException {
    return new RelationalAnnotation(text, name,//
            new Range(rs.getLong(tableName + "_range_start"), rs.getLong(tableName + "_range_end")),//
            rs.getBytes(tableName + "_json_data"), rs.getLong(tableName + "_id"));
  }

  public static RelationalText mapTextFrom(ResultSet rs, String prefix, RelationalAnnotation layer) throws SQLException {
    return new RelationalText(layer, Text.Type.values()[rs.getInt(prefix + "_type")],//
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
