package eu.interedition.text.rdbms;

import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import eu.interedition.text.*;
import eu.interedition.text.mem.SimpleQName;
import eu.interedition.text.predicate.*;
import eu.interedition.text.util.AbstractAnnotationRepository;
import eu.interedition.text.util.SQL;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
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

import static com.google.common.collect.Iterables.isEmpty;
import static eu.interedition.text.rdbms.RelationalQNameRepository.mapNameFrom;
import static eu.interedition.text.rdbms.RelationalQNameRepository.selectNameFrom;
import static eu.interedition.text.rdbms.RelationalTextRepository.mapTextFrom;
import static eu.interedition.text.rdbms.RelationalTextRepository.selectTextFrom;

public class RelationalAnnotationRepository extends AbstractAnnotationRepository {

  private SimpleJdbcTemplate jt;
  private RelationalQNameRepository nameRepository;
  private RelationalTextRepository textRepository;
  private SimpleJdbcInsert annotationInsert;
  private SimpleJdbcInsert annotationSetInsert;
  private SimpleJdbcInsert annotationLinkTargetInsert;
  private SAXParserFactory saxParserFactory;
  private int batchSize = 10000;

  public void setDataSource(DataSource dataSource) {
    this.jt = (dataSource == null ? null : new SimpleJdbcTemplate(dataSource));
    this.annotationInsert = (jt == null ? null : new SimpleJdbcInsert(dataSource).withTableName("text_annotation").usingGeneratedKeyColumns("id"));
    this.annotationSetInsert = (jt == null ? null : new SimpleJdbcInsert(dataSource).withTableName("text_annotation_link").usingGeneratedKeyColumns("id"));
    this.annotationLinkTargetInsert = (jt == null ? null : new SimpleJdbcInsert(dataSource).withTableName("text_annotation_link_target"));
  }

  public void setNameRepository(RelationalQNameRepository nameRepository) {
    this.nameRepository = nameRepository;
  }

  public void setTextRepository(RelationalTextRepository textRepository) {
    this.textRepository = textRepository;
  }

  public void setBatchSize(int batchSize) {
    this.batchSize = batchSize;
  }

  public Annotation create(Text text, QName name, Range range) {
    final RelationalAnnotation created = new RelationalAnnotation();
    created.setText((RelationalText) text);
    created.setName(nameRepository.get(name));
    created.setRange(range == null ? Range.NULL : range);

    final HashMap<String, Object> annotationData = Maps.newHashMap();
    annotationData.put("text", ((RelationalText) text).getId());
    annotationData.put("name", ((RelationalQName) nameRepository.get(name)).getId());
    annotationData.put("range_start", range.getStart());
    annotationData.put("range_end", range.getEnd());
    created.setId(annotationInsert.executeAndReturnKey(annotationData).intValue());

    return created;
  }

  public AnnotationLink createLink(QName name) {
    RelationalQName relationalQName = (RelationalQName) nameRepository.get(name);

    final Map<String, Object> setData = Maps.newHashMap();
    setData.put("name", relationalQName.getId());

    return new RelationalAnnotationLink(annotationSetInsert.executeAndReturnKey(setData).intValue(), relationalQName);
  }

  public void delete(Iterable<AnnotationPredicate> predicates) {
    final ArrayList<Object> parameters = new ArrayList<Object>();
    final List<Object[]> batchParameters = Lists.newArrayListWithCapacity(batchSize);

    jt.query(buildAnnotationFinderSQL("select a.id as a_id", parameters, predicates).toString(), new RowMapper<Void>() {
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

  public void deleteLinks(Iterable<Predicate> predicates) {
    final ArrayList<Object> parameters = new ArrayList<Object>();
    final List<Object[]> batchParameters = Lists.newArrayListWithCapacity(batchSize);
    jt.query(buildAnnotationLinkFinderSQL("select distinct al.id as al_id", parameters, predicates).toString(), new RowMapper<Void>() {
      public Void mapRow(ResultSet rs, int rowNum) throws SQLException {
        batchParameters.add(new Object[] { rs.getInt("al_id") });

        if (rs.isLast() || (batchParameters.size() % batchSize) == 0) {
          jt.batchUpdate("delete from text_annotation_link where id = ?", batchParameters);
          batchParameters.clear();
        }

        return null;
      }
    }, parameters.toArray(new Object[parameters.size()]));
  }

  @SuppressWarnings("unchecked")
  public void add(AnnotationLink to, Set<Annotation> toAdd) {
    if (toAdd == null || toAdd.isEmpty()) {
      return;
    }
    final int setId = ((RelationalAnnotationLink) to).getId();
    final List<Map<String, Object>> psList = new ArrayList<Map<String, Object>>(toAdd.size());
    for (RelationalAnnotation annotation : Iterables.filter(toAdd, RelationalAnnotation.class)) {
      final Map<String, Object> ps = new HashMap<String, Object>(2);
      ps.put("link", setId);
      ps.put("target", annotation.getId());
      psList.add(ps);
    }
    annotationLinkTargetInsert.executeBatch(psList.toArray(new Map[psList.size()]));
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

  public Iterable<Annotation> find(Iterable<AnnotationPredicate> predicates) {
    List<Object> parameters = Lists.newArrayList();

    final StringBuilder sql = buildAnnotationFinderSQL(new StringBuilder("select ").
            append(selectAnnotationFrom("a")).append(", ").
            append(selectNameFrom("n")).append(", ").
            append(selectTextFrom("t")).toString(), parameters, predicates);
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

  public Map<AnnotationLink, Set<Annotation>> findLinks(Iterable<Predicate> predicates) {
    final List<Object> params = new ArrayList<Object>();
    final StringBuilder sql = buildAnnotationLinkFinderSQL(new StringBuilder("select ")
            .append("al.id as al_id, ")
            .append(selectAnnotationFrom("a")).append(", ")
            .append(selectTextFrom("t")).append(", ")
            .append(selectNameFrom("aln")).append(", ")
            .append(selectNameFrom("an")).toString(), params, predicates);
    sql.append(" order by al.id, t.id, an.id, a.id");

    final Map<AnnotationLink, Set<Annotation>> annotationLinks = new HashMap<AnnotationLink, Set<Annotation>>();

    jt.query(sql.toString(), new RowMapper<Void>() {
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

    }, params.toArray(new Object[params.size()]));

    return annotationLinks;
  }

  private StringBuilder buildAnnotationFinderSQL(String selectClause, List<Object> parameters, Iterable<AnnotationPredicate> predicates) {
    final StringBuilder sql = new StringBuilder(selectClause);
    sql.append(" from text_annotation a");
    sql.append(" join text_qname n on a.name = n.id");
    sql.append(" join text_content t on a.text = t.id");

    if (!isEmpty(predicates)) {
      sql.append(" where 1=1");
    }

    final Iterable<AnnotationIdentityPredicate> equalsPredicates = Iterables.filter(predicates, AnnotationIdentityPredicate.class);
    if (!isEmpty(equalsPredicates)) {
      sql.append(" and a.id in (");
      for (Iterator<AnnotationIdentityPredicate> it = equalsPredicates.iterator(); it.hasNext(); ) {
        parameters.add(((RelationalAnnotation) it.next().getAnnotation()).getId());
        sql.append("?").append(it.hasNext() ? ", " : "");
      }
      sql.append(")");
    }

    final Iterable<TextPredicate> textPredicates = Iterables.filter(predicates, TextPredicate.class);
    if (!isEmpty(textPredicates)) {
      sql.append(" and a.text in (");
      for (Iterator<TextPredicate> it = textPredicates.iterator(); it.hasNext(); ) {
        parameters.add(((RelationalText) it.next().getText()).getId());
        sql.append("?").append(it.hasNext() ? ", " : "");
      }
      sql.append(")");
    }

    final Iterable<AnnotationNamePredicate> namePredicates = Iterables.filter(predicates, AnnotationNamePredicate.class);
    if (!isEmpty(namePredicates)) {
      final Set<QName> names = Sets.newHashSet(Iterables.transform(namePredicates, AnnotationNamePredicate.TO_NAME));

      sql.append(" and a.name in (");
      for (Iterator<QName> it = nameRepository.get(names).iterator(); it.hasNext(); ) {
        parameters.add(((RelationalQName) it.next()).getId());
        sql.append("?").append(it.hasNext() ? ", " : "");
      }
      sql.append(")");
    }

    final Iterable<TextRangePredicate> rangePredicates = Iterables.filter(predicates, TextRangePredicate.class);
    if (!isEmpty(rangePredicates)) {
      sql.append(" and (");
      for (Iterator<TextRangePredicate> it = rangePredicates.iterator(); it.hasNext(); ) {
        final TextRangePredicate predicate = it.next();

        sql.append("(a.text = ? and a.range_start < ? and a.range_end > ?)");
        sql.append(it.hasNext() ? " or " : "");

        final Range range = predicate.getRange();
        parameters.add(((RelationalText)predicate.getText()).getId());
        parameters.add(range.getEnd());
        parameters.add(range.getStart());
      }
      sql.append(")");
    }

    return sql;
  }

  private StringBuilder buildAnnotationLinkFinderSQL(String selectClause, List<Object> params, Iterable<Predicate> predicates) {
    final StringBuilder sql = new StringBuilder(selectClause);
    sql.append(" from text_annotation_link_target alt");
    sql.append(" join text_annotation_link al on alt.link = al.id");
    sql.append(" join text_qname aln on al.name = aln.id");
    sql.append(" join text_annotation a on alt.target = a.id");
    sql.append(" join text_qname an on a.name = an.id");
    sql.append(" join text_content t on a.text = t.id");

    if (!isEmpty(predicates)) {
      sql.append(" where 1=1");
    }

    final Iterable<TextPredicate> textPredicates = Iterables.filter(predicates, TextPredicate.class);
    if (!isEmpty(textPredicates)) {
      sql.append(" and a.text in (");
      for (Iterator<TextPredicate> it = textPredicates.iterator(); it.hasNext(); ) {
        params.add(((RelationalText) it.next().getText()).getId());
        sql.append("?").append(it.hasNext() ? ", " : "");
      }
      sql.append(")");
    }

    final Iterable<AnnotationLinkNamePredicate> linkNamePredicates = Iterables.filter(predicates, AnnotationLinkNamePredicate.class);
    if (!isEmpty(linkNamePredicates)) {
      final Set<QName> linkNames = Sets.newHashSet(Iterables.transform(linkNamePredicates, AnnotationLinkNamePredicate.TO_NAME));
      sql.append(" and al.name in (");
      for (Iterator<QName> it = nameRepository.get(linkNames).iterator(); it.hasNext(); ) {
        params.add(((RelationalQName) it.next()).getId());
        sql.append("?").append(it.hasNext() ? ", " : "");
      }
      sql.append(")");
    }

    final Iterable<AnnotationNamePredicate> namePredicates = Iterables.filter(predicates, AnnotationNamePredicate.class);
    if (!isEmpty(namePredicates)) {
      final Set<QName> names = Sets.newHashSet(Iterables.transform(namePredicates, AnnotationNamePredicate.TO_NAME));

      sql.append(" and a.name in (");
      for (Iterator<QName> it = nameRepository.get(names).iterator(); it.hasNext(); ) {
        params.add(((RelationalQName) it.next()).getId());
        sql.append("?").append(it.hasNext() ? ", " : "");
      }
      sql.append(")");
    }

    final Iterable<TextRangePredicate> rangePredicates = Iterables.filter(predicates, TextRangePredicate.class);
    if (!isEmpty(rangePredicates)) {
      sql.append(" and (");
      for (Iterator<TextRangePredicate> it = rangePredicates.iterator(); it.hasNext(); ) {
        final TextRangePredicate predicate = it.next();

        sql.append("(a.text = ? and a.range_start < ? and a.range_end > ?)");
        sql.append(it.hasNext() ? " or " : "");

        final Range range = predicate.getRange();
        params.add(((RelationalText)predicate.getText()).getId());
        params.add(range.getEnd());
        params.add(range.getStart());
      }
      sql.append(")");
    }
    return sql;
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
              saxParserFactory().newSAXParser().parse(new InputSource(content), new DefaultHandler() {
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

  public static String selectAnnotationFrom(String tableName) {
    return SQL.select(tableName, "id", "range_start", "range_end");
  }


  public static RelationalAnnotation mapAnnotationFrom(ResultSet rs, RelationalText text, RelationalQName name, String tableName) throws SQLException {
    final RelationalAnnotation relationalAnnotation = new RelationalAnnotation();
    relationalAnnotation.setId(rs.getInt(tableName + "_id"));
    relationalAnnotation.setName(name);
    relationalAnnotation.setText(text);
    relationalAnnotation.setRange(new Range(rs.getInt(tableName + "_range_start"), rs.getInt(tableName + "_range_end")));
    return relationalAnnotation;
  }

  protected SAXParserFactory saxParserFactory() {
    if (saxParserFactory == null) {
      saxParserFactory = SAXParserFactory.newInstance();
      saxParserFactory.setNamespaceAware(true);
      saxParserFactory.setValidating(false);
    }
    return saxParserFactory;
  }
}
