package eu.interedition.text.rdbms;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.MapMaker;
import com.google.common.collect.Sets;
import eu.interedition.text.QName;
import eu.interedition.text.QNameRepository;
import eu.interedition.text.util.SQL;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import javax.sql.DataSource;
import java.net.URI;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class RelationalQNameRepository implements QNameRepository {

  private DataSource dataSource;
  private SimpleJdbcTemplate jt;

  private Map<QName, RelationalQName> nameCache;

  private int cacheSize = 1000;

  public void setDataSource(DataSource dataSource) {
    this.dataSource = dataSource;
    this.jt = (dataSource == null ? null : new SimpleJdbcTemplate(dataSource));
  }

  public void setCacheSize(int cacheSize) {
    this.cacheSize = cacheSize;
  }

  public QName get(QName name) {
    return Iterables.getOnlyElement(get(Collections.singleton(name)));
  }

  public Set<QName> load(Set<Integer> ids) {
    if (ids == null || ids.isEmpty()) {
      return Sets.newHashSet();
    }

    final List<Integer> ps = Lists.newArrayList(ids);
    final StringBuilder sql = new StringBuilder("select ").append(select("n")).append(" from text_qname n where n.id in (");
    for (int i = 0; i < ps.size(); i++) {
      sql.append(i == 0 ? "" : ", ").append("?");
    }
    sql.append(")");

    return new HashSet<QName>(jt.query(sql.toString(), ROW_MAPPER, ps.toArray(new Object[ps.size()])));
  }

  public synchronized Set<QName> get(Set<QName> names) {
    if (nameCache == null) {
      initCache();
    }

    names = Sets.newHashSet(names);
    final Set<QName> foundNames = Sets.newHashSetWithExpectedSize(names.size());

    for (Iterator<QName> it = names.iterator(); it.hasNext(); ) {
      final RelationalQName relationalQName = nameCache.get(it.next());
      if (relationalQName != null) {
        foundNames.add(relationalQName);
        it.remove();
      }
    }

    if (!names.isEmpty()) {
      final List<Object> ps = Lists.newArrayList();
      final StringBuilder sql = new StringBuilder("select ").append(select("n")).append(" from text_qname n where ");
      for (Iterator<QName> it = names.iterator(); it.hasNext(); ) {
        sql.append("(");
        final QName name = it.next();

        sql.append("n.local_name = ? and ");
        ps.add(name.getLocalName());

        final URI ns = name.getNamespaceURI();
        if (ns == null) {
          sql.append("n.namespace is null");
        } else {
          sql.append("n.namespace = ?");
          ps.add(ns.toString());
        }


        sql.append(")").append(it.hasNext() ? " or " : "");
      }

      for (RelationalQName name : jt.query(sql.toString(), ROW_MAPPER, ps.toArray(new Object[ps.size()]))) {
        foundNames.add(name);
        names.remove(name);
        nameCache.put(name, name);
      }

      final SimpleJdbcInsert insert = new SimpleJdbcInsert(dataSource).withTableName("text_qname").usingGeneratedKeyColumns("id");
      for (QName name : names) {
        final String localName = name.getLocalName();
        final URI ns = name.getNamespaceURI();

        final Map<String, Object> namePs = new HashMap<String, Object>();
        namePs.put("local_name", localName);
        namePs.put("namespace", ns == null ? null : ns.toString());

        final RelationalQName created = new RelationalQName(ns, localName);
        created.setId(insert.executeAndReturnKey(namePs).intValue());

        foundNames.add(created);
        nameCache.put(created, created);
      }
    }

    return foundNames;
  }

  private void initCache() {
    nameCache = new MapMaker().maximumSize(cacheSize).makeMap();
    if (jt.queryForInt("select count(*) from text_qname") <= cacheSize) {
      // warm-up cache
      for (RelationalQName name : jt.query("select " + select("n") + " from text_qname n", ROW_MAPPER)) {
        nameCache.put(name, name);
      }
    }
  }

  public synchronized void clearCache() {
    nameCache = null;
  }

  static String select(String tableName) {
    return SQL.select(tableName, "id", "local_name", "namespace");
  }

  static RelationalQName mapName(ResultSet rs, String prefix) throws SQLException {
    final RelationalQName name = new RelationalQName();
    name.setId(rs.getInt(prefix + "_id"));
    name.setLocalName(rs.getString(prefix + "_local_name"));
    name.setNamespace(rs.getString(prefix + "_namespace"));
    return name;
  }

  private static final RowMapper<RelationalQName> ROW_MAPPER = new RowMapper<RelationalQName>() {

    public RelationalQName mapRow(ResultSet rs, int rowNum) throws SQLException {
      return mapName(rs, "n");
    }
  };
}
