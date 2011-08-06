package eu.interedition.text.rdbms;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import eu.interedition.text.Annotation;
import eu.interedition.text.AnnotationLink;
import eu.interedition.text.QName;
import eu.interedition.text.query.Criterion;
import eu.interedition.text.util.AbstractAnnotationLinkRepository;
import eu.interedition.text.util.SQL;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.jdbc.support.incrementer.DataFieldMaxValueIncrementer;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static eu.interedition.text.rdbms.RelationalAnnotationRepository.mapAnnotationFrom;
import static eu.interedition.text.rdbms.RelationalAnnotationRepository.selectAnnotationFrom;
import static eu.interedition.text.rdbms.RelationalQNameRepository.mapNameFrom;
import static eu.interedition.text.rdbms.RelationalQNameRepository.selectNameFrom;
import static eu.interedition.text.rdbms.RelationalTextRepository.selectTextFrom;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class RelationalAnnotationLinkRepository extends AbstractAnnotationLinkRepository implements InitializingBean {
  private DataSource dataSource;
  private DataFieldMaxValueIncrementerFactory incrementerFactory;
  private RelationalQNameRepository nameRepository;
  private RelationalQueryCriteriaTranslator queryCriteriaTranslator;

  private int batchSize = 10000;

  private SimpleJdbcTemplate jt;
  private SimpleJdbcInsert annotationLinkInsert;
  private SimpleJdbcInsert annotationLinkTargetInsert;
  private SimpleJdbcInsert annotationLinkDataInsert;
  private DataFieldMaxValueIncrementer annotationLinkIdIncrementer;

  public Iterable<AnnotationLink> create(Iterable<QName> names) {
    final Map<QName, Long> nameIdIndex = Maps.newHashMap();
    for (QName n : nameRepository.get(Sets.newHashSet(names))) {
      nameIdIndex.put(n, ((RelationalQName) n).getId());
    }

    final List<AnnotationLink> created = Lists.newArrayList();
    final List<SqlParameterSource> batchParameters = Lists.newArrayList();
    for (QName n : names) {
      final long id = annotationLinkIdIncrementer.nextLongValue();
      final Long nameId = nameIdIndex.get(n);

      batchParameters.add(new MapSqlParameterSource()
              .addValue("id", id)
              .addValue("name", nameId));

      created.add(new RelationalAnnotationLink(id, new RelationalQName(nameId, n)));
    }

    annotationLinkInsert.executeBatch(batchParameters.toArray(new SqlParameterSource[batchParameters.size()]));
    return created;
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
            .append(selectNameFrom("aln")).append(", ")
            .append(selectNameFrom("an")).toString();

    final StringBuilder where = new StringBuilder("al.id in (");
    for (Iterator<Long> linkIdIt = linkIds.iterator(); linkIdIt.hasNext(); ) {
      where.append("?").append(linkIdIt.hasNext() ? ", " : "");
    }
    where.append(")");

    final Map<AnnotationLink, Set<Annotation>> annotationLinks = new HashMap<AnnotationLink, Set<Annotation>>();
    jt.query(sql(dataSelect, where.toString()).append(" order by al.id, t.id, an.id, a.id").toString(), new RowMapper<Void>() {
      private RelationalAnnotationLink currentLink;
      private RelationalText currentText;
      private RelationalQName currentAnnotationName;

      public Void mapRow(ResultSet rs, int rowNum) throws SQLException {
        final int annotationLinkId = rs.getInt("al_id");
        final int textId = rs.getInt("t_id");
        final int annotationNameId = rs.getInt("an_id");

        if (currentLink == null || currentLink.getId() != annotationLinkId) {
          currentLink = new RelationalAnnotationLink(annotationLinkId, mapNameFrom(rs, "aln"));
        }
        if (currentText == null || currentText.getId() != textId) {
          currentText = RelationalTextRepository.mapTextFrom(rs, "t");
        }
        if (currentAnnotationName == null || currentAnnotationName.getId() != annotationNameId) {
          currentAnnotationName = mapNameFrom(rs, "an");
        }

        Set<Annotation> members = annotationLinks.get(currentLink);
        if (members == null) {
          annotationLinks.put(currentLink, members = new TreeSet<Annotation>());
        }
        members.add(mapAnnotationFrom(rs, currentText, currentAnnotationName, "a"));

        return null;
      }

    }, linkIds.toArray(new Object[linkIds.size()]));

    return annotationLinks;
  }

  public void add(AnnotationLink to, Set<Annotation> toAdd) {
    if (toAdd == null || toAdd.isEmpty()) {
      return;
    }
    final long linkId = ((RelationalAnnotationLink) to).getId();
    final List<SqlParameterSource> psList = new ArrayList<SqlParameterSource>(toAdd.size());
    for (RelationalAnnotation annotation : Iterables.filter(toAdd, RelationalAnnotation.class)) {
      psList.add(new MapSqlParameterSource().addValue("link", linkId).addValue("target", annotation.getId()));
    }
    annotationLinkTargetInsert.executeBatch(psList.toArray(new SqlParameterSource[psList.size()]));
  }

  public void remove(AnnotationLink from, Set<Annotation> toRemove) {
    if (toRemove == null || toRemove.isEmpty()) {
      return;
    }

    final StringBuilder sql = new StringBuilder("delete from text_annotation_link_target where link = ? and ");

    final List<Object> params = new ArrayList<Object>(toRemove.size() + 1);
    params.add(((RelationalAnnotationLink) from).getId());

    final Set<RelationalAnnotation> annotations = Sets.newHashSet(Iterables.filter(toRemove, RelationalAnnotation.class));
    sql.append("target in (");
    for (Iterator<RelationalAnnotation> it = annotations.iterator(); it.hasNext(); ) {
      params.add(it.next().getId());
      sql.append("?").append(it.hasNext() ? ", " : "");
    }
    sql.append(")");

    jt.update(sql.toString(), params.toArray(new Object[params.size()]));
  }

  public void set(AnnotationLink link, Map<QName, String> data) {
    final long linkId = ((RelationalAnnotationLink) link).getId();
    final List<SqlParameterSource> batchParams = new ArrayList<SqlParameterSource>(data.size());
    for (QName name : nameRepository.get(data.keySet())) {
      final long nameId = ((RelationalQName) name).getId();
      if (jt.update("update text_annotation_link_data set value = ? where link = ? and name = ?",
              data.get(name), linkId, nameId) == 0) {
        batchParams.add(new MapSqlParameterSource()
                .addValue("link", linkId)
                .addValue("name", nameId)
                .addValue("value", data.get(name)));
      }
    }
    if (!batchParams.isEmpty()) {
      annotationLinkDataInsert.executeBatch(batchParams.toArray(new SqlParameterSource[batchParams.size()]));
    }
  }

  public Map<QName, String> get(AnnotationLink link) {
    final Map<QName, String> data = new HashMap<QName, String>();
    final StringBuilder sql = new StringBuilder("select  ");
    sql.append(selectDataFrom("d")).append(", ");
    sql.append(RelationalQNameRepository.selectNameFrom("n"));
    sql.append(" from text_annotation_link_data d join text_qname n on d.name = n.id where link = ?");
    jt.query(sql.toString(), new RowMapper<Void>() {

      public Void mapRow(ResultSet rs, int rowNum) throws SQLException {
        data.put(RelationalQNameRepository.mapNameFrom(rs, "n"), mapDataFrom(rs, "d"));
        return null;
      }
    }, ((RelationalAnnotationLink) link).getId());
    return data;
  }

  public void delete(AnnotationLink link, Set<QName> names) {
    List<Object> params = new ArrayList<Object>(names.size() + 1);
    StringBuilder sql = new StringBuilder("delete from text_annotation_link_data where link = ?");
    params.add(((RelationalAnnotationLink) link).getId());

    if (names != null && !names.isEmpty()) {
      sql.append(" and name in (");
      for (Iterator<RelationalQName> it = Iterables.filter(nameRepository.get(names), RelationalQName.class).iterator(); it.hasNext(); ) {
        params.add(it.next().getId());
        sql.append("?").append(it.hasNext() ? ", " : "");
      }
      sql.append(")");
    }

    jt.update(sql.toString(), params.toArray(new Object[params.size()]));
  }

  @Required
  public void setDataSource(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  @Required
  public void setIncrementerFactory(DataFieldMaxValueIncrementerFactory incrementerFactory) {
    this.incrementerFactory = incrementerFactory;
  }

  @Required
  public void setNameRepository(RelationalQNameRepository nameRepository) {
    this.nameRepository = nameRepository;
  }

  @Required
  public void setQueryCriteriaTranslator(RelationalQueryCriteriaTranslator queryCriteriaTranslator) {
    this.queryCriteriaTranslator = queryCriteriaTranslator;
  }

  public void setBatchSize(int batchSize) {
    this.batchSize = batchSize;
  }

  public void afterPropertiesSet() throws Exception {
    this.jt = (dataSource == null ? null : new SimpleJdbcTemplate(dataSource));
    this.annotationLinkInsert = (jt == null ? null : new SimpleJdbcInsert(dataSource).withTableName("text_annotation_link"));
    this.annotationLinkTargetInsert = (jt == null ? null : new SimpleJdbcInsert(dataSource).withTableName("text_annotation_link_target"));
    this.annotationLinkDataInsert = new SimpleJdbcInsert(dataSource).withTableName("text_annotation_link_data");
    this.annotationLinkIdIncrementer = incrementerFactory.create("text_annotation_link");
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
    return sql;
  }

  public static String selectDataFrom(String tableName) {
    return SQL.select(tableName, "value");
  }

  public static String mapDataFrom(ResultSet rs, String prefix) throws SQLException {
    return rs.getString(prefix + "_value");
  }
}
