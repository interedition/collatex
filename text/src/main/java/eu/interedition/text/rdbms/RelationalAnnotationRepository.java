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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import eu.interedition.text.Annotation;
import eu.interedition.text.AnnotationConsumer;
import eu.interedition.text.Name;
import eu.interedition.text.Range;
import eu.interedition.text.Text;
import eu.interedition.text.query.Criterion;
import eu.interedition.text.util.AbstractAnnotationRepository;
import eu.interedition.text.util.SQL;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.incrementer.DataFieldMaxValueIncrementer;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import static eu.interedition.text.rdbms.RelationalNameRepository.mapNameFrom;
import static eu.interedition.text.rdbms.RelationalNameRepository.selectNameFrom;
import static eu.interedition.text.rdbms.RelationalTextRepository.mapTextFrom;
import static eu.interedition.text.rdbms.RelationalTextRepository.selectTextFrom;

public class RelationalAnnotationRepository extends AbstractAnnotationRepository implements InitializingBean {

  private DataSource dataSource;
  private RelationalDatabaseKeyFactory keyFactory;
  private RelationalNameRepository nameRepository;
  private RelationalQueryCriteriaTranslator queryCriteriaTranslator;

  private JdbcTemplate jt;
  private SimpleJdbcInsert annotationInsert;
  private SimpleJdbcInsert annotationDataInsert;

  private DataFieldMaxValueIncrementer annotationIdIncrementer;

  public Iterable<Annotation> create(Iterable<Annotation> annotations) {
    final Set<Name> names = Sets.newHashSet();
    for (Annotation a : annotations) {
      names.add(a.getName());
      names.addAll(a.getData().keySet());
    }
    final Map<Name, Long> nameIds = Maps.newHashMap();
    for (Name name : nameRepository.get(names)) {
      nameIds.put(name, ((RelationalName)name).getId());
    }

    final List<Annotation> created = Lists.newArrayList();
    final List<SqlParameterSource> annotationBatch = Lists.newArrayList();
    final List<SqlParameterSource> dataBatch = new ArrayList<SqlParameterSource>();
    for (Annotation a : annotations) {
      final long id = annotationIdIncrementer.nextLongValue();
      final Long nameId = nameIds.get(a.getName());
      final Range range = a.getRange();

      annotationBatch.add(new MapSqlParameterSource()
              .addValue("id", id)
              .addValue("text", ((RelationalText) a.getText()).getId())
              .addValue("name", nameId)
              .addValue("range_start", range.getStart())
              .addValue("range_end", range.getEnd()));
      created.add(new RelationalAnnotation(a.getText(), new RelationalName(a.getName(), nameId), range, a.getData(), id));

      for (Map.Entry<Name, String> dataEntry : a.getData().entrySet()) {
        dataBatch.add(new MapSqlParameterSource()
                .addValue("annotation", id)
                .addValue("name", nameIds.get(dataEntry.getKey()))
                .addValue("value", dataEntry.getValue()));
      }
    }

    if (!annotationBatch.isEmpty()) {
      annotationInsert.executeBatch(annotationBatch.toArray(new SqlParameterSource[annotationBatch.size()]));
    }
    if (!dataBatch.isEmpty()) {
      annotationDataInsert.executeBatch(dataBatch.toArray(new SqlParameterSource[dataBatch.size()]));
    }

    return created;
  }

  @Override
  public void scroll(Criterion criterion, final AnnotationConsumer consumer) {
    final List<Object> parameters = Lists.newArrayList();

    final StringBuilder sql = sql(new StringBuilder("select ").
            append(selectAnnotationFrom("a")).append(", ").
            append(selectNameFrom("n")).append(", ").
            append(selectTextFrom("t")).toString(), false, parameters, criterion);
    sql.append(" order by n.id");

    jt.query(sql.toString(), new RowMapper<Void>() {
      private Map<Long, RelationalText> textCache = Maps.newHashMap();
      private RelationalName currentName;

      public Void mapRow(ResultSet rs, int rowNum) throws SQLException {
        if (currentName == null || currentName.getId() != rs.getLong("n_id")) {
          currentName = mapNameFrom(rs, "n");
        }
        final long textId = rs.getLong("t_id");
        RelationalText currentText = textCache.get(textId);
        if (currentText == null) {
          textCache.put(textId, currentText = mapTextFrom(rs, "t"));
        }

        consumer.consume(mapAnnotationFrom(rs, currentText, currentName, "a"));
        return null;
      }
    }, parameters.toArray(new Object[parameters.size()]));
  }

  @Override
  public void scroll(Criterion criterion, final Set<Name> names, final AnnotationConsumer consumer) {
    if (names != null && names.isEmpty()) {
      scroll(criterion, consumer);
      return;
    }

    final List<Object> ps = Lists.newArrayList();
    final StringBuilder sql = sql(new StringBuilder("select  ")
            .append(selectAnnotationFrom("a")).append(", ")
            .append(selectNameFrom("n")).append(", ")
            .append(selectTextFrom("t")).append(", ")
            .append(selectNameFrom("dn")).append(", ")
            .append(selectDataFrom("d")).toString(), true, ps, criterion);

    sql.append(" order by a.id, n.id, dn.id ");


    jt.query(sql.toString(), new RowMapper<Void>() {
      private final Map<Long, Name> nameCache = Maps.newHashMap();
      private final Map<Long, Text> textCache = Maps.newHashMap();

      private Text text;
      private Name name;
      private Range range;
      private long id;
      private Map<Name, String> data;

      public Void mapRow(ResultSet rs, int rowNum) throws SQLException {
        final long annotationId = rs.getLong("a_id");
        if (id == 0 || id != annotationId) {
          if (id != 0) {
            consumer.consume(new RelationalAnnotation(text, name, range, data, id));
          }
          final long textId = rs.getLong("t_id");
          text = textCache.get(textId);
          if (text == null) {
            textCache.put(textId, text = mapTextFrom(rs, "t"));
          }
          name = name(rs.getLong("n_id"), rs, "n");
          range = new Range(rs.getLong("a_range_start"), rs.getLong("a_range_end"));
          data = Maps.newHashMap();
          id = annotationId;
        }

        final long dataNameId = rs.getLong("dn_id");
        if (dataNameId != 0) {
          final Name dataName = name(dataNameId, rs, "dn");
          if (names == null || names.contains(dataName)) {
            data.put(dataName, mapDataFrom(rs, "d"));
          }
        }

        if (rs.isLast()) {
          consumer.consume(new RelationalAnnotation(text, name, range, data, id));
        }
        return null;
      }

      private Name name(long id, ResultSet rs, String prefix) throws SQLException {
        Name name = nameCache.get(id);
        if (name == null) {
          nameCache.put(id, name = mapNameFrom(rs, prefix));
        }
        return name;
      }
    }, ps.toArray(new Object[ps.size()]));
  }


  public void delete(Criterion criterion) {
    final ArrayList<Object> parameters = new ArrayList<Object>();
    final List<Object[]> batchParameters = Lists.newArrayListWithCapacity(batchSize);

    jt.query(sql("select a.id as a_id", false, parameters, criterion).toString(), new RowMapper<Void>() {
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

  @Required
  public void setDataSource(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  @Required
  public void setKeyFactory(RelationalDatabaseKeyFactory keyFactory) {
    this.keyFactory = keyFactory;
  }

  @Required
  public void setNameRepository(RelationalNameRepository nameRepository) {
    this.nameRepository = nameRepository;
  }

  @Required
  public void setQueryCriteriaTranslator(RelationalQueryCriteriaTranslator queryCriteriaTranslator) {
    this.queryCriteriaTranslator = queryCriteriaTranslator;
  }

  public void afterPropertiesSet() throws Exception {
    super.afterPropertiesSet();

    this.jt = (dataSource == null ? null : new JdbcTemplate(dataSource));
    this.annotationInsert = (jt == null ? null : new SimpleJdbcInsert(dataSource).withTableName("text_annotation"));
    this.annotationDataInsert = new SimpleJdbcInsert(dataSource).withTableName("text_annotation_data");

    this.annotationIdIncrementer = keyFactory.create("text_annotation");
  }

  private StringBuilder sql(String select, boolean joinData, List<Object> ps, Criterion criterion) {
    return queryCriteriaTranslator.where(from(new StringBuilder(select), joinData), criterion, ps);
  }

  private StringBuilder from(StringBuilder sql, boolean joinData) {
    sql.append(" from text_annotation a");
    sql.append(" join text_qname n on a.name = n.id");
    sql.append(" join text_content t on a.text = t.id");
    if (joinData) {
      sql.append(" left join text_annotation_data d on d.annotation = a.id");
      sql.append(" left join text_qname dn on d.name = dn.id");
    }
    return sql;
  }

  public static String selectAnnotationFrom(String tableName) {
    return SQL.select(tableName, "id", "range_start", "range_end");
  }


  public static RelationalAnnotation mapAnnotationFrom(ResultSet rs, RelationalText text, RelationalName name, String tableName) throws SQLException {
    return new RelationalAnnotation(text, name,//
            new Range(rs.getLong(tableName + "_range_start"), rs.getLong(tableName + "_range_end")), null,//
            rs.getLong(tableName + "_id"));
  }

  public static String selectDataFrom(String tableName) {
    return SQL.select(tableName, "value");
  }

  public static String mapDataFrom(ResultSet rs, String prefix) throws SQLException {
    return rs.getString(prefix + "_value");
  }
}
