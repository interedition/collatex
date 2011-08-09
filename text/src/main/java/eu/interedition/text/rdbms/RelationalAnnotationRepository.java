package eu.interedition.text.rdbms;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import eu.interedition.text.*;
import eu.interedition.text.mem.SimpleQName;
import eu.interedition.text.query.*;
import eu.interedition.text.util.AbstractAnnotationRepository;
import eu.interedition.text.util.SQL;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.jdbc.support.incrementer.DataFieldMaxValueIncrementer;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.sql.DataSource;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.Reader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static eu.interedition.text.rdbms.RelationalQNameRepository.mapNameFrom;
import static eu.interedition.text.rdbms.RelationalQNameRepository.selectNameFrom;
import static eu.interedition.text.rdbms.RelationalTextRepository.mapTextFrom;
import static eu.interedition.text.rdbms.RelationalTextRepository.selectTextFrom;

public class RelationalAnnotationRepository extends AbstractAnnotationRepository implements InitializingBean {

  private DataSource dataSource;
  private DataFieldMaxValueIncrementerFactory incrementerFactory;
  private RelationalQNameRepository nameRepository;
  private RelationalTextRepository textRepository;
  private RelationalQueryCriteriaTranslator queryCriteriaTranslator;

  private SimpleJdbcTemplate jt;
  private SimpleJdbcInsert annotationInsert;
  private SimpleJdbcInsert annotationDataInsert;
  private SAXParserFactory saxParserFactory;

  private int batchSize = 10000;
  private DataFieldMaxValueIncrementer annotationIdIncrementer;

  public Iterable<Annotation> create(Iterable<Annotation> annotations) {
    final Set<QName> names = Sets.newHashSet();
    for (Annotation a : annotations) {
      names.add(a.getName());
    }
    final Map<QName, Long> nameIdIndex = Maps.newHashMapWithExpectedSize(names.size());
    for (QName name : nameRepository.get(names)) {
      nameIdIndex.put(name, ((RelationalQName) name).getId());
    }

    final List<Annotation> created = Lists.newArrayList();
    final List<SqlParameterSource> batchParameters = Lists.newArrayList();
    for (Annotation a : annotations) {
      final long id = annotationIdIncrementer.nextLongValue();
      final Long nameId = nameIdIndex.get(a.getName());
      final Range range = a.getRange();

      batchParameters.add(new MapSqlParameterSource()
              .addValue("id", id)
              .addValue("text", ((RelationalText) a.getText()).getId())
              .addValue("name", nameId)
              .addValue("range_start", range.getStart())
              .addValue("range_end", range.getEnd()));

      final RelationalAnnotation ra = new RelationalAnnotation();
      ra.setId(id);
      ra.setText(a.getText());
      ra.setName(new RelationalQName(nameId, a.getName()));
      ra.setRange(range);
      created.add(ra);
    }

    annotationInsert.executeBatch(batchParameters.toArray(new SqlParameterSource[batchParameters.size()]));

    return created;
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

  public void delete(Criterion criterion) {
    final ArrayList<Object> parameters = new ArrayList<Object>();
    final List<Object[]> batchParameters = Lists.newArrayListWithCapacity(batchSize);

    jt.query(sql("select a.id as a_id", parameters, criterion).toString(), new RowMapper<Void>() {
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


  public Iterable<Annotation> find(Criterion criterion) {
    List<Object> parameters = Lists.newArrayList();

    final StringBuilder sql = sql(new StringBuilder("select ").
            append(selectAnnotationFrom("a")).append(", ").
            append(selectNameFrom("n")).append(", ").
            append(selectTextFrom("t")).toString(), parameters, criterion);
    sql.append(" order by n.id");

    return Sets.newTreeSet(jt.query(sql.toString(), new RowMapper<Annotation>() {
      private RelationalQName currentName;

      public Annotation mapRow(ResultSet rs, int rowNum) throws SQLException {
        if (currentName == null || currentName.getId() != rs.getInt("n_id")) {
          currentName = mapNameFrom(rs, "n");
        }
        return mapAnnotationFrom(rs, mapTextFrom(rs, "t"), currentName, "a");
      }
    }, parameters.toArray(new Object[parameters.size()])));
  }

  public SortedSet<QName> names(Text text) {
    final StringBuilder namesSql = new StringBuilder("select distinct ");
    namesSql.append(selectNameFrom("n"));
    namesSql.append(" from text_qname n join text_annotation a on a.name = n.id where a.text = ?");
    final SortedSet<QName> names = Sets.newTreeSet(jt.query(namesSql.toString(), new RowMapper<QName>() {

      public QName mapRow(ResultSet rs, int rowNum) throws SQLException {
        return mapNameFrom(rs, "n");
      }
    }, ((RelationalText) text).getId()));

    if (names.isEmpty() && text.getType() == Text.Type.XML) {
      try {
        textRepository.read(text, new TextRepository.TextReader() {
          public void read(Reader content, int contentLength) throws IOException {
            if (contentLength == 0) {
              return;
            }
            try {
              saxParserFactory.newSAXParser().parse(new InputSource(content), new DefaultHandler() {
                @Override
                public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                  names.add(new SimpleQName(uri, localName));
                }
              });
            } catch (SAXException e) {
              throw Throwables.propagate(e);
            } catch (ParserConfigurationException e) {
              throw Throwables.propagate(e);
            }
          }
        });
      } catch (IOException e) {
        throw Throwables.propagate(e);
      }
    }

    return names;
  }

  public Map<Annotation, Map<QName, String>> get(Iterable<Annotation> links, Set<QName> names) {
    final Map<Long, RelationalAnnotation> annotationIds = Maps.newHashMap();
    for (Annotation link : links) {
      RelationalAnnotation rl = (RelationalAnnotation)link;
      annotationIds.put(rl.getId(), rl);
    }

    if (annotationIds.isEmpty()) {
      return Collections.emptyMap();
    }

    final List<Long> ps = Lists.newArrayList(annotationIds.keySet());
    final StringBuilder sql = new StringBuilder("select  ");
    sql.append(selectDataFrom("d")).append(", ");
    sql.append(RelationalQNameRepository.selectNameFrom("n")).append(", ");
    sql.append("d.annotation as d_annotation");
    sql.append(" from text_annotation_data d join text_qname n on d.name = n.id where d.annotation in (");
    for (Iterator<Long> annotationIdIt = annotationIds.keySet().iterator(); annotationIdIt.hasNext(); ) {
      annotationIdIt.next();
      sql.append("?").append(annotationIdIt.hasNext() ? ", " : "");
    }
    sql.append(")");

    if (!names.isEmpty()) {
      sql.append(" and d.name in (");
      for (Iterator<QName> nameIt = nameRepository.get(names).iterator(); nameIt.hasNext(); ) {
        ps.add(((RelationalQName)nameIt.next()).getId());
        sql.append("?").append(nameIt.hasNext() ? ", " : "");
      }
      sql.append(")");
    }

    sql.append(" order by d.annotation");

    final Map<Annotation, Map<QName, String>> data = new HashMap<Annotation, Map<QName, String>>();
    for (RelationalAnnotation annotation : annotationIds.values()) {
      data.put(annotation, Maps.<QName, String>newHashMap());
    }

    final Map<Long, RelationalQName> nameCache = Maps.newHashMap();
    jt.query(sql.toString(), new RowMapper<Void>() {

      private RelationalAnnotation annotation;
      private Map<QName, String> dataMap;

      public Void mapRow(ResultSet rs, int rowNum) throws SQLException {
        final long annotationId = rs.getLong("d_annotation");
        if (annotation == null || annotation.getId() != annotationId) {
          annotation = annotationIds.get(annotationId);
          dataMap = data.get(annotation);
        }

        RelationalQName name = RelationalQNameRepository.mapNameFrom(rs, "n");
        if (nameCache.containsKey(name.getId())) {
          name = nameCache.get(name.getId());
        } else {
          nameCache.put(name.getId(), name);
        }

        dataMap.put(name, mapDataFrom(rs, "d"));

        return null;
      }
    }, ps.toArray(new Object[ps.size()]));

    return data;
  }

  public void set(Map<Annotation, Map<QName, String>> data) {
    final Set<QName> names = Sets.newHashSet();
    for (Map<QName, String> dataEntry : data.values()) {
      for (QName name : dataEntry.keySet()) {
        names.add(name);
      }
    }
    final Map<QName, Long> nameIds = Maps.newHashMap();
    for (QName name : nameRepository.get(names)) {
      nameIds.put(name, ((RelationalQName)name).getId());
    }

    final List<SqlParameterSource> batchParams = new ArrayList<SqlParameterSource>(data.size());
    for (Annotation annotation : data.keySet()) {
      final long annotationId = ((RelationalAnnotation) annotation).getId();
      final Map<QName, String> annotationData = data.get(annotation);
      for (Map.Entry<QName, String> dataEntry : annotationData.entrySet()) {
        batchParams.add(new MapSqlParameterSource()
                .addValue("annotation", annotationId)
                .addValue("name", nameIds.get(dataEntry.getKey()))
                .addValue("value", dataEntry.getValue()));
      }
    }

    if (!batchParams.isEmpty()) {
      annotationDataInsert.executeBatch(batchParams.toArray(new SqlParameterSource[batchParams.size()]));
    }
  }

  public void unset(Map<Annotation, Iterable<QName>> data) {
    final Set<QName> names = Sets.newHashSet();
    for (Iterable<QName> linkNames : data.values()) {
      for (QName name : linkNames) {
        names.add(name);
      }
    }

    final Map<QName, Long> nameIds = Maps.newHashMapWithExpectedSize(names.size());
    for (QName name : nameRepository.get(names)) {
      nameIds.put(name, ((RelationalQName)name).getId());
    }

    List<SqlParameterSource> batchPs = Lists.newArrayList();
    for (Map.Entry<Annotation, Iterable<QName>> dataEntry : data.entrySet()) {
      long annotationId = ((RelationalAnnotation) dataEntry.getKey()).getId();
      for (QName name : dataEntry.getValue()) {
        batchPs.add(new MapSqlParameterSource()
        .addValue("annotation", annotationId)
        .addValue("name", nameIds.get(name)));
      }
    }

    jt.batchUpdate("delete from text_annotation_data where annotation = :annotation and name = :name", batchPs.toArray(new SqlParameterSource[batchPs.size()]));
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
  public void setTextRepository(RelationalTextRepository textRepository) {
    this.textRepository = textRepository;
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
    this.annotationInsert = (jt == null ? null : new SimpleJdbcInsert(dataSource).withTableName("text_annotation"));
    this.annotationDataInsert = new SimpleJdbcInsert(dataSource).withTableName("text_annotation_data");

    this.saxParserFactory = SAXParserFactory.newInstance();
    this.saxParserFactory.setNamespaceAware(true);
    this.saxParserFactory.setValidating(false);

    this.annotationIdIncrementer = incrementerFactory.create("text_annotation");
  }

  private StringBuilder sql(String select, List<Object> ps, Criterion criterion) {
    return queryCriteriaTranslator.where(from(new StringBuilder(select)), criterion, ps);
  }

  private StringBuilder from(StringBuilder sql) {
    sql.append(" from text_annotation a");
    sql.append(" join text_qname n on a.name = n.id");
    sql.append(" join text_content t on a.text = t.id");
    return sql;
  }

  public static String selectAnnotationFrom(String tableName) {
    return SQL.select(tableName, "id", "range_start", "range_end");
  }


  public static RelationalAnnotation mapAnnotationFrom(ResultSet rs, RelationalText text, RelationalQName name, String tableName) throws SQLException {
    final RelationalAnnotation ra = new RelationalAnnotation();
    ra.setId(rs.getInt(tableName + "_id"));
    ra.setName(name);
    ra.setText(text);
    ra.setRange(new Range(rs.getInt(tableName + "_range_start"), rs.getInt(tableName + "_range_end")));
    return ra;
  }

  public static String selectDataFrom(String tableName) {
    return SQL.select(tableName, "value");
  }

  public static String mapDataFrom(ResultSet rs, String prefix) throws SQLException {
    return rs.getString(prefix + "_value");
  }
}
