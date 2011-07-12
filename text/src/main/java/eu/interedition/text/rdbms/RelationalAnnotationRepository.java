package eu.interedition.text.rdbms;

import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import eu.interedition.text.*;
import eu.interedition.text.util.AbstractAnnotationRepository;
import eu.interedition.text.util.QNameImpl;
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

public class RelationalAnnotationRepository extends AbstractAnnotationRepository {

  private SimpleJdbcTemplate jt;
  private RelationalQNameRepository nameRepository;
  private RelationalTextRepository textRepository;
  private SimpleJdbcInsert annotationInsert;
  private SimpleJdbcInsert annotationSetInsert;
  private SimpleJdbcInsert annotationSetMemberInsert;
  private SAXParserFactory saxParserFactory;

  public void setDataSource(DataSource dataSource) {
    this.jt = (dataSource == null ? null : new SimpleJdbcTemplate(dataSource));
    this.annotationInsert = (jt == null ? null : new SimpleJdbcInsert(dataSource).withTableName("text_annotation").usingGeneratedKeyColumns("id"));
    this.annotationSetInsert = (jt == null ? null : new SimpleJdbcInsert(dataSource).withTableName("text_annotation_set").usingGeneratedKeyColumns("id"));
    this.annotationSetMemberInsert = (jt == null ? null : new SimpleJdbcInsert(dataSource).withTableName("text_annotation_set_member"));
  }

  public void setNameRepository(RelationalQNameRepository nameRepository) {
    this.nameRepository = nameRepository;
  }

  public void setTextRepository(RelationalTextRepository textRepository) {
    this.textRepository = textRepository;
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

  public AnnotationSet createSet(QName name) {
    RelationalQName relationalQName = (RelationalQName) nameRepository.get(name);

    final Map<String, Object> setData = Maps.newHashMap();
    setData.put("name", relationalQName.getId());

    return new RelationalAnnotationSet(annotationSetInsert.executeAndReturnKey(setData).intValue(), relationalQName);
  }

  public void delete(Annotation annotation) {
    jt.update("delete from text_annotation where id = ?", ((RelationalAnnotation) annotation).getId());
  }

  public void delete(AnnotationSet annotationSet) {
    jt.update("delete from text_annotation_set where id = ?", ((RelationalAnnotationSet) annotationSet).getId());
  }

  public void add(AnnotationSet to, Set<Annotation> toAdd) {
    if (toAdd == null || toAdd.isEmpty()) {
      return;
    }
    final int setId = ((RelationalAnnotationSet) to).getId();
    final List<Map<String, Object>> psList = new ArrayList<Map<String, Object>>(toAdd.size());
    for (RelationalAnnotation annotation : Iterables.filter(toAdd, RelationalAnnotation.class)) {
      final Map<String, Object> ps = new HashMap<String, Object>(2);
      ps.put("annotation_set", setId);
      ps.put("annotation", annotation.getId());
      psList.add(ps);
    }
    annotationSetMemberInsert.executeBatch(psList.toArray(new Map[psList.size()]));
  }

  public void remove(AnnotationSet from, Set<Annotation> toRemove) {
    if (toRemove == null || toRemove.isEmpty()) {
      return;
    }

    final StringBuilder sql = new StringBuilder("delete from text_annotation_set_member where annotation_set = ? AND ");

    final List<Object> params = new ArrayList<Object>(toRemove.size() + 1);
    params.add(((RelationalAnnotationSet) from).getId());

    final Set<RelationalAnnotation> annotations = Sets.newHashSet(Iterables.filter(toRemove, RelationalAnnotation.class));
    sql.append("annotation in (");
    for (Iterator<RelationalAnnotation> it = annotations.iterator(); it.hasNext(); ) {
      params.add(it.next().getId());
      sql.append("?").append(it.hasNext() ? ", " : "");
    }
    sql.append(")");

    jt.update(sql.toString(), params.toArray(new Object[params.size()]));
  }

  @SuppressWarnings("unchecked")
  public Iterable<Annotation> find(final Text text, Set<QName> names, Set<Range> ranges, boolean overlapping) {
    final RelationalText relationalText = (RelationalText) text;

    List<Object> parameters = Lists.newArrayList();

    final StringBuilder sql = new StringBuilder("select ");
    sql.append(select("a")).append(", ");
    sql.append(RelationalQNameRepository.select("n"));
    sql.append(" from text_annotation a join text_qname n on a.name = n.id where a.text = ?");
    parameters.add(((RelationalText) text).getId());

    if (names != null && !names.isEmpty()) {
      sql.append(" and a.name in (");
      for (Iterator<QName> it = nameRepository.get(names).iterator(); it.hasNext(); ) {
        parameters.add(((RelationalQName) it.next()).getId());
        sql.append("?").append(it.hasNext() ? ", " : "");
      }
      sql.append(")");
    }

    if (ranges != null && !ranges.isEmpty()) {
      sql.append(" and (");
      for (Iterator<Range> it = ranges.iterator(); it.hasNext(); ) {
        final Range range = it.next();
        sql.append("a.range_start < ? and a.range_end > ?");
        sql.append(it.hasNext() ? " or " : "");
        parameters.add(range.getEnd());
        parameters.add(range.getStart());
      }
      sql.append(")");
    }

    sql.append(" order by n.id");

    return Sets.newTreeSet(jt.query(sql.toString(), new RowMapper<Annotation>() {
      private RelationalQName currentName;

      public Annotation mapRow(ResultSet rs, int rowNum) throws SQLException {
        if (currentName == null || currentName.getId() != rs.getInt("n_id")) {
          currentName = RelationalQNameRepository.mapName(rs, "n");
        }
        return mapAnnotation(rs, relationalText, currentName, "a");
      }
    }, parameters.toArray(new Object[parameters.size()])));
  }

  public Map<AnnotationSet, Set<Annotation>> findSets(Set<Text> texts, Set<QName> setNames, Set<QName> names, Set<Range> ranges) {
    final List<Object> params = new ArrayList<Object>();
    final StringBuilder sql = new StringBuilder("select ");
    sql.append("as.id as as_id, ");
    sql.append(select("a")).append(", ");
    sql.append(RelationalTextRepository.select("t")).append(", ");
    sql.append(RelationalQNameRepository.select("asn")).append(", ");
    sql.append(RelationalQNameRepository.select("an"));
    sql.append(" from text_annotation_set_member asm");
    sql.append(" join text_annotation_set as on asm.annotation_set = as.id");
    sql.append(" join text_qname asn on as.name = asn.id");
    sql.append(" join text_annotation a on asm.member");
    sql.append(" join text_qname an on a.name = an.id");
    sql.append(" join text_content t on a.text = t.id");

    final boolean textCriteria = texts != null && !texts.isEmpty();
    final boolean setNameCriteria = setNames != null && !setNames.isEmpty();
    final boolean nameCriteria = names != null && !names.isEmpty();
    final boolean rangeCritera = ranges != null && !ranges.isEmpty();
    boolean firstCriteria = true;

    if (textCriteria || setNameCriteria || nameCriteria || rangeCritera) {
      sql.append(" where");
    }
    if (textCriteria) {
      sql.append(" a.text in (");
      for (Iterator<Text> it = texts.iterator(); it.hasNext(); ) {
        params.add(((RelationalText)it.next()).getId());
        sql.append("?").append(it.hasNext() ? ", " : "");
      }
      sql.append(")");
      firstCriteria = false;
    }

    if (setNameCriteria) {
      setNames = nameRepository.get(setNames);
      if (!firstCriteria) {
        sql.append(" and");
      }
      sql.append(" asm.name in (");
      for (Iterator<QName> it = setNames.iterator(); it.hasNext(); ) {
        params.add(((RelationalQName)it.next()).getId());
        sql.append("?").append(it.hasNext() ? ", " : "");
      }
      sql.append(")");
      firstCriteria = false;
    }

    if (nameCriteria) {
      names = nameRepository.get(names);
      if (!firstCriteria) {
        sql.append(" and");
      }
      sql.append(" a.name in (");
      for (Iterator<QName> it = names.iterator(); it.hasNext(); ) {
        params.add(((RelationalQName)it.next()).getId());
        sql.append("?").append(it.hasNext() ? ", " : "");
      }
      sql.append(")");
      firstCriteria = false;
    }

    if (rangeCritera) {
      if (!firstCriteria) {
        sql.append(" and");
      }

      sql.append("(");
      for (Iterator<Range> it = ranges.iterator(); it.hasNext(); ) {
        final Range range = it.next();
        params.add(range.getEnd());
        params.add(range.getStart());
        sql.append(" (a.range_start < ? and a.range_end > ?)");
        sql.append(it.hasNext() ? " or" : "");
      }
      sql.append(")");
    }

    sql.append(" order by as.id, t.id, an.id, a.id");

    final Map<AnnotationSet, Set<Annotation>> annotationSets = new HashMap<AnnotationSet, Set<Annotation>>();

    jt.query(sql.toString(), new RowMapper<Void>() {
      private RelationalAnnotationSet currentSet;
      private RelationalText currentText;
      private RelationalQName currentAnnotationName;

      public Void mapRow(ResultSet rs, int rowNum) throws SQLException {
        final int annotationSetId = rs.getInt("as_id");
        final int textId = rs.getInt("t_id");
        final int annotationNameId = rs.getInt("an_id");

        if (currentSet == null || currentSet.getId() != annotationSetId) {
          currentSet = new RelationalAnnotationSet(annotationSetId, RelationalQNameRepository.mapName(rs, "asn"));
        }
        if (currentText == null || currentText.getId() != textId) {
          currentText = RelationalTextRepository.mapText(rs, "t");
        }
        if (currentAnnotationName == null || currentAnnotationName.getId() != annotationNameId) {
          currentAnnotationName = RelationalQNameRepository.mapName(rs, "an");
        }

        Set<Annotation> members = annotationSets.get(currentSet);
        if (members == null) {
          annotationSets.put(currentSet, members = new TreeSet<Annotation>());
        }
        members.add(mapAnnotation(rs, currentText, currentAnnotationName, "a"));

        return null;
      }

    }, params.toArray(new Object[params.size()]));

    return annotationSets;
  }

  public SortedSet<QName> names(Text text) {
    final SortedSet<QName> names = Sets.newTreeSet(jt.query("select distinct n.id as n_id, n.local_name as n_local_name, n.namespace as n_namespace from text_qname n join text_annotation a on a.name = n.id where a.text = ?", new RowMapper<QName>() {

      public QName mapRow(ResultSet rs, int rowNum) throws SQLException {
        return RelationalQNameRepository.mapName(rs, "n");
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
                  names.add(new QNameImpl(uri, localName));
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

  static String select(String tableName) {
    return Util.select(tableName, "id", "range_start", "range_end");
  }


  static RelationalAnnotation mapAnnotation(ResultSet rs, RelationalText text, RelationalQName name, String tableName) throws SQLException {
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
