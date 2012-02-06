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
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import eu.interedition.text.Annotation;
import eu.interedition.text.AnnotationLink;
import eu.interedition.text.Name;
import eu.interedition.text.query.Criterion;
import eu.interedition.text.util.AbstractAnnotationLinkRepository;
import eu.interedition.text.util.SQL;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.incrementer.DataFieldMaxValueIncrementer;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static eu.interedition.text.rdbms.RelationalTextRepository.mapAnnotationFrom;
import static eu.interedition.text.rdbms.RelationalTextRepository.selectAnnotationFrom;
import static eu.interedition.text.rdbms.RelationalNameRegistry.mapNameFrom;
import static eu.interedition.text.rdbms.RelationalNameRegistry.selectNameFrom;
import static eu.interedition.text.rdbms.RelationalTextRepository.mapTextFrom;
import static eu.interedition.text.rdbms.RelationalTextRepository.selectTextFrom;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class RelationalAnnotationLinkRepository extends AbstractAnnotationLinkRepository implements InitializingBean {
  private DataSource dataSource;
  private RelationalDatabaseKeyFactory keyFactory;
  private RelationalNameRegistry nameRepository;
  private RelationalQueryCriteriaTranslator queryCriteriaTranslator;

  private int batchSize = 10000;

  private JdbcTemplate jt;
  private NamedParameterJdbcTemplate npjt;
  private SimpleJdbcInsert annotationLinkInsert;
  private SimpleJdbcInsert annotationLinkTargetInsert;
  private SimpleJdbcInsert annotationLinkDataInsert;
  private DataFieldMaxValueIncrementer annotationLinkIdIncrementer;

  public Map<AnnotationLink, Set<Annotation>> create(Multimap<Name, Set<Annotation>> links) {
    final Map<Name, Long> nameIdIndex = Maps.newHashMap();
    for (Name n : nameRepository.get(links.keySet())) {
      nameIdIndex.put(n, ((RelationalName) n).getId());
    }

    final Map<AnnotationLink, Set<Annotation>> created = Maps.newLinkedHashMap();
    final List<SqlParameterSource> linkBatch = Lists.newArrayList();
    final List<SqlParameterSource> targetBatch = Lists.newArrayList();

    for (Map.Entry<Name, Set<Annotation>> link : links.entries()) {
      final Name linkName = link.getKey();
      final Set<Annotation> targets = link.getValue();

      final Long nameId = nameIdIndex.get(linkName);
      final long linkId = annotationLinkIdIncrementer.nextLongValue();

      linkBatch.add(new MapSqlParameterSource()
              .addValue("id", linkId)
              .addValue("name", nameId));

      for (Annotation target : targets) {
          targetBatch.add(new MapSqlParameterSource()
                  .addValue("link", linkId)
                  .addValue("target", ((RelationalAnnotation) target).getId()));
      }

      created.put(new RelationalAnnotationLink(new RelationalName(linkName, nameId), linkId), targets);
    }

    annotationLinkInsert.executeBatch(linkBatch.toArray(new SqlParameterSource[linkBatch.size()]));
    annotationLinkTargetInsert.executeBatch(targetBatch.toArray(new SqlParameterSource[targetBatch.size()]));

    return created;
  }

  public void delete(Iterable<AnnotationLink> links) {
    final List<Long> linkIds = Lists.newArrayList();
    for (AnnotationLink a : links) {
      linkIds.add(((RelationalAnnotationLink)a).getId());
    }
    if (linkIds.isEmpty()) {
      return;
    }
    final StringBuilder sql = new StringBuilder("delete from text_annotation_link where id in (");
    for (Iterator<Long> idIt = linkIds.iterator(); idIt.hasNext(); ) {
      idIt.next();
      sql.append("?").append(idIt.hasNext() ? ", " : "");
    }
    sql.append(")");
    jt.update(sql.toString(), linkIds.toArray(new Object[linkIds.size()]));
  }

  public void delete(Criterion criterion) {
    final ArrayList<Object> parameters = new ArrayList<Object>();
    final List<Object[]> batchParameters = Lists.newArrayListWithCapacity(batchSize);
    jt.query(sql("select distinct al.id as al_id", parameters, criterion).toString(), new RowMapper<Void>() {
      public Void mapRow(ResultSet rs, int rowNum) throws SQLException {
        batchParameters.add(new Object[]{rs.getInt("al_id")});

        if (rs.isLast() || (batchParameters.size() % batchSize) == 0) {
          jt.batchUpdate("delete from text_annotation_link where id = ?", batchParameters);
          batchParameters.clear();
        }

        return null;
      }
    }, parameters.toArray(new Object[parameters.size()]));
  }

  public void cleanup() {
    StringBuilder sql = new StringBuilder();
    sql.append("select distinct al.id as link_id");
    sql.append(" from text_annotation_link al");
    sql.append(" left join text_annotation_link_target alt on al.id = alt.link");
    sql.append(" where alt.target is null");

    final List<SqlParameterSource> ids = Lists.newArrayList();
    jt.query(sql.toString(), new RowMapper<Void>() {
      public Void mapRow(ResultSet rs, int rowNum) throws SQLException {
        ids.add(new MapSqlParameterSource().addValue("id", rs.getLong("link_id")));
        return null;
      }
    });

    if (!ids.isEmpty()) {
      npjt.batchUpdate("delete from text_annotation_link where id = :id", ids.toArray(new SqlParameterSource[ids.size()]));
    }
  }

  public Map<AnnotationLink, Set<Annotation>> find(Criterion criterion) {
    final List<Long> linkIds = Lists.newArrayList();

    final List<Object> ps = new ArrayList<Object>();
    jt.query(sql("select distinct al.id as al_id", ps, criterion).toString(), new RowMapper<Void>() {
      public Void mapRow(ResultSet rs, int rowNum) throws SQLException {
        linkIds.add(rs.getLong("al_id"));
        return null;
      }
    }, ps.toArray(new Object[ps.size()]));

    if (linkIds.isEmpty()) {
      return Collections.emptyMap();
    }

    final String dataSelect = new StringBuilder("select ")
            .append("al.id as al_id, ")
            .append(selectAnnotationFrom("a")).append(", ")
            .append(selectTextFrom("t")).append(", ")
            .append(selectAnnotationFrom("l")).append(", ")
            .append(selectNameFrom("ln")).append(", ")
            .append(selectNameFrom("aln")).append(", ")
            .append(selectNameFrom("an")).toString();

    final StringBuilder where = new StringBuilder("al.id in (");
    for (Iterator<Long> linkIdIt = linkIds.iterator(); linkIdIt.hasNext(); ) {
      linkIdIt.next();
      where.append("?").append(linkIdIt.hasNext() ? ", " : "");
    }
    where.append(")");

    final Map<AnnotationLink, Set<Annotation>> annotationLinks = new HashMap<AnnotationLink, Set<Annotation>>();
    jt.query(sql(dataSelect, where.toString()).append(" order by al.id, t.id, an.id, a.id").toString(), new RowMapper<Void>() {
      private final Map<Long, RelationalName> nameCache = Maps.newHashMap();
      private final Map<Long, RelationalText> textCache = Maps.newHashMap();

      private RelationalAnnotationLink currentLink;
      private RelationalText currentText;
      private RelationalName currentAnnotationName;

      public Void mapRow(ResultSet rs, int rowNum) throws SQLException {
        final int annotationLinkId = rs.getInt("al_id");
        final int textId = rs.getInt("t_id");
        final int annotationNameId = rs.getInt("an_id");

        if (currentLink == null || currentLink.getId() != annotationLinkId) {
          currentLink = new RelationalAnnotationLink(mapNameFrom(rs, "aln"), annotationLinkId);
        }
        if (currentText == null || currentText.getId() != textId) {
          currentText = text(textId, rs);
        }
        if (currentAnnotationName == null || currentAnnotationName.getId() != annotationNameId) {
          currentAnnotationName = name(annotationNameId, rs, "an");
        }

        Set<Annotation> members = annotationLinks.get(currentLink);
        if (members == null) {
          annotationLinks.put(currentLink, members = new TreeSet<Annotation>());
        }
        members.add(mapAnnotationFrom(rs, currentText, currentAnnotationName, "a"));

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

    }, linkIds.toArray(new Object[linkIds.size()]));

    return annotationLinks;
  }

  public Map<AnnotationLink, Map<Name, String>> get(Iterable<AnnotationLink> links, Set<Name> names) {
    final Map<Long, RelationalAnnotationLink> linkIds = Maps.newHashMap();
    for (AnnotationLink link : links) {
      RelationalAnnotationLink rl = (RelationalAnnotationLink)link;
      linkIds.put(rl.getId(), rl);
    }

    if (linkIds.isEmpty()) {
      return Collections.emptyMap();
    }

    final List<Object> ps = Lists.<Object>newArrayList(linkIds.keySet());
    final StringBuilder sql = new StringBuilder("select  ");
    sql.append(selectDataFrom("d")).append(", ");
    sql.append(RelationalNameRegistry.selectNameFrom("n")).append(", ");
    sql.append("d.link as d_link");
    sql.append(" from text_annotation_link_data d join text_qname n on d.name = n.id where d.link in (");
    for (Iterator<Long> linkIdIt = linkIds.keySet().iterator(); linkIdIt.hasNext(); ) {
      sql.append("?").append(linkIdIt.hasNext() ? ", " : "");
    }
    sql.append(")");

    if (!names.isEmpty()) {
      sql.append(" and d.name in (");
      for (Iterator<Name> nameIt = nameRepository.get(names).iterator(); nameIt.hasNext(); ) {
        ps.add(((RelationalName)nameIt.next()).getId());
        sql.append("?").append(nameIt.hasNext() ? ", " : "");
      }
      sql.append(")");
    }

    sql.append(" order by d.link");

    final Map<AnnotationLink, Map<Name, String>> data = new HashMap<AnnotationLink, Map<Name, String>>();
    for (RelationalAnnotationLink link : linkIds.values()) {
      data.put(link, Maps.<Name, String>newHashMap());
    }

    final Map<Long, RelationalName> nameCache = Maps.newHashMap();
    jt.query(sql.toString(), new RowMapper<Void>() {

      private RelationalAnnotationLink link;
      private Map<Name, String> dataMap;

      public Void mapRow(ResultSet rs, int rowNum) throws SQLException {
        final long linkId = rs.getLong("d_link");
        if (link == null || link.getId() != linkId) {
          link = linkIds.get(linkId);
          dataMap = data.get(link);
        }

        RelationalName name = RelationalNameRegistry.mapNameFrom(rs, "n");
        if (nameCache.containsKey(name.getId())) {
          name = nameCache.get(name.getId());
        } else {
          nameCache.put(name.getId(), name);
        }

        dataMap.put(name, mapDataFrom(rs, "d"));

        return null;
      }
    }, ps);

    return data;
  }

  public void set(Map<AnnotationLink, Map<Name, String>> data) {
    final Set<Name> names = Sets.newHashSet();
    for (Map<Name, String> dataEntry : data.values()) {
      for (Name name : dataEntry.keySet()) {
        names.add(name);
      }
    }
    final Map<Name, Long> nameIds = Maps.newHashMap();
    for (Name name : nameRepository.get(names)) {
      nameIds.put(name, ((RelationalName)name).getId());
    }

    final List<SqlParameterSource> batchParams = new ArrayList<SqlParameterSource>(data.size());
    for (AnnotationLink link : data.keySet()) {
      final long linkId = ((RelationalAnnotationLink) link).getId();
      final Map<Name, String> linkData = data.get(link);
      for (Map.Entry<Name, String> dataEntry : linkData.entrySet()) {
        batchParams.add(new MapSqlParameterSource()
                .addValue("link", linkId)
                .addValue("name", nameIds.get(dataEntry.getKey()))
                .addValue("value", dataEntry.getValue()));
      }
    }

    if (!batchParams.isEmpty()) {
      annotationLinkDataInsert.executeBatch(batchParams.toArray(new SqlParameterSource[batchParams.size()]));
    }
  }

  public void unset(Map<AnnotationLink, Iterable<Name>> data) {
    final Set<Name> names = Sets.newHashSet();
    for (Iterable<Name> linkNames : data.values()) {
      for (Name name : linkNames) {
        names.add(name);
      }
    }

    final Map<Name, Long> nameIds = Maps.newHashMapWithExpectedSize(names.size());
    for (Name name : nameRepository.get(names)) {
      nameIds.put(name, ((RelationalName)name).getId());
    }

    List<SqlParameterSource> batchPs = Lists.newArrayList();
    for (Map.Entry<AnnotationLink, Iterable<Name>> dataEntry : data.entrySet()) {
      long linkId = ((RelationalAnnotationLink) dataEntry.getKey()).getId();
      for (Name name : dataEntry.getValue()) {
        batchPs.add(new MapSqlParameterSource()
        .addValue("link", linkId)
        .addValue("name", nameIds.get(name)));
      }
    }

    npjt.batchUpdate("delete from text_annotation_link_data where link = :link and name = :name", batchPs.toArray(new SqlParameterSource[batchPs.size()]));
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
  public void setNameRepository(RelationalNameRegistry nameRegistry) {
    this.nameRepository = nameRegistry;
  }

  @Required
  public void setQueryCriteriaTranslator(RelationalQueryCriteriaTranslator queryCriteriaTranslator) {
    this.queryCriteriaTranslator = queryCriteriaTranslator;
  }

  public void setBatchSize(int batchSize) {
    this.batchSize = batchSize;
  }

  public void afterPropertiesSet() throws Exception {
    this.jt = (dataSource == null ? null : new JdbcTemplate(dataSource));
    this.npjt = (jt == null ? null : new NamedParameterJdbcTemplate(jt));
    this.annotationLinkInsert = (jt == null ? null : new SimpleJdbcInsert(dataSource).withTableName("text_annotation_link"));
    this.annotationLinkTargetInsert = (jt == null ? null : new SimpleJdbcInsert(dataSource).withTableName("text_annotation_link_target"));
    this.annotationLinkDataInsert = new SimpleJdbcInsert(dataSource).withTableName("text_annotation_link_data");
    this.annotationLinkIdIncrementer = keyFactory.create("text_annotation_link");
  }

  private StringBuilder sql(String select, String where) {
    return from(new StringBuilder(select)).append(" where ").append(where);
  }

  private StringBuilder sql(String select, List<Object> ps, Criterion criterion) {
    return queryCriteriaTranslator.where(from(new StringBuilder(select)), criterion, ps);
  }

  private StringBuilder from(StringBuilder sql) {
    sql.append(" from text_annotation_link_target alt");
    sql.append(" join text_annotation_link al on alt.link = al.id");
    sql.append(" join text_qname aln on al.name = aln.id");
    sql.append(" join text_annotation a on alt.target = a.id");
    sql.append(" join text_qname an on a.name = an.id");
    sql.append(" join text_content t on a.text = t.id");
    sql.append(" left join text_annotation l on t.layer = l.id");
    sql.append(" left join text_qname ln on l.name = ln.id");
    return sql;
  }

  public static String selectDataFrom(String tableName) {
    return SQL.select(tableName, "value");
  }

  public static String mapDataFrom(ResultSet rs, String prefix) throws SQLException {
    return rs.getString(prefix + "_value");
  }
}
