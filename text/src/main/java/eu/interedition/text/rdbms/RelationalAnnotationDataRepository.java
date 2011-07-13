package eu.interedition.text.rdbms;

import com.google.common.collect.Iterables;
import eu.interedition.text.Annotation;
import eu.interedition.text.QName;
import eu.interedition.text.QNameRepository;
import eu.interedition.text.util.AbstractAnnotationDataRepository;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class RelationalAnnotationDataRepository extends AbstractAnnotationDataRepository implements InitializingBean {
  private QNameRepository nameRepository;
  private DataSource dataSource;
  private SimpleJdbcTemplate jt;
  private SimpleJdbcInsert dataInsert;

  @Required
  public void setNameRepository(QNameRepository nameRepository) {
    this.nameRepository = nameRepository;
  }

  @Required
  public void setDataSource(DataSource dataSource) {
    this.dataSource = dataSource;
    this.jt = (dataSource == null ? null : new SimpleJdbcTemplate(dataSource));
  }

  public void set(Annotation annotation, Map<QName, String> data) {
    final int annotationId = ((RelationalAnnotation) annotation).getId();
    final List<Map<String, Object>> batchParams = new ArrayList<Map<String, Object>>(data.size());
    for (QName name : nameRepository.get(data.keySet())) {
      final int nameId = ((RelationalQName) name).getId();
      if (jt.update("update text_annotation_data set value = ? where annotation = ? and name = ?",
              data.get(name), annotationId, nameId) == 0) {
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("annotation", annotationId);
        params.put("name", nameId);
        params.put("value", data.get(name));
        batchParams.add(params);
      }
    }
    if (!batchParams.isEmpty()) {
      dataInsert.executeBatch(batchParams.toArray(new Map[batchParams.size()]));
    }
  }

  public Map<QName, String> get(Annotation annotation) {
    final Map<QName, String> data = new HashMap<QName, String>();
    final StringBuilder sql = new StringBuilder("select d.value as d_value, ");
    sql.append(RelationalQNameRepository.select("n"));
    sql.append(" from text_annotation_data d join text_qname n on d.name = n.id where annotation = ?");
    jt.query(sql.toString(), new RowMapper<Void>() {

              public Void mapRow(ResultSet rs, int rowNum) throws SQLException {
                data.put(RelationalQNameRepository.mapName(rs, "n"), rs.getString("d_value"));
                return null;
              }
            }, ((RelationalAnnotation)annotation).getId());
    return data;
  }

  public void delete(Annotation annotation, Set<QName> names) {
    List<Object> params = new ArrayList<Object>(names.size() + 1);
    StringBuilder sql = new StringBuilder("delete from text_annotation_data where annotation = ?");
    params.add(((RelationalAnnotation)annotation).getId());

    if (names != null && !names.isEmpty()) {
      sql.append(" and name in (");
      for (Iterator<RelationalQName> it = Iterables.filter(nameRepository.get(names), RelationalQName.class).iterator(); it.hasNext();) {
          params.add(it.next().getId());
          sql.append("?").append(it.hasNext() ? ", " : "");
      }
      sql.append(")");
    }

    jt.update(sql.toString(), params.toArray(new Object[params.size()]));
  }

  public void afterPropertiesSet() throws Exception {
     dataInsert = new SimpleJdbcInsert(dataSource).withTableName("text_annotation_data");
  }
}
