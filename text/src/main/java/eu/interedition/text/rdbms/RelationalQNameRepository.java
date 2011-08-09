package eu.interedition.text.rdbms;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.MapMaker;
import com.google.common.collect.Sets;
import eu.interedition.text.QName;
import eu.interedition.text.QNameRepository;
import eu.interedition.text.QNameSet;
import eu.interedition.text.mem.SimpleQNameSet;
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
import java.net.URI;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static com.google.common.collect.Iterables.getFirst;
import static java.util.Collections.singleton;

public class RelationalQNameRepository implements QNameRepository, InitializingBean {

  private DataSource dataSource;
  private DataFieldMaxValueIncrementerFactory incrementerFactory;
  private int cacheSize = 1000;

  private SimpleJdbcTemplate jt;
  private SimpleJdbcInsert nameInsert;
  private SimpleJdbcInsert nameSetInsert;
  private DataFieldMaxValueIncrementer nameIdIncrementer;

  private Map<QName, Long> nameCache;

  public QName get(QName name) {
    return Iterables.getOnlyElement(get(singleton(name)));
  }

  public Set<QName> load(Set<Integer> ids) {
    if (ids == null || ids.isEmpty()) {
      return Sets.newHashSet();
    }

    final List<Integer> ps = Lists.newArrayList(ids);
    final StringBuilder sql = new StringBuilder("select ").append(selectNameFrom("n")).append(" from text_qname n where n.id in (");
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
      final QName name = it.next();
      final Long id = nameCache.get(name);
      if (id != null) {
        foundNames.add(new RelationalQName(id, name));
        it.remove();
      }
    }

    if (!names.isEmpty()) {
      final List<Object> ps = Lists.newArrayList();
      final StringBuilder sql = new StringBuilder("select ").append(selectNameFrom("n")).append(" from text_qname n where ");
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
        nameCache.put(name, name.getId());
      }

      final List<RelationalQName> created = Lists.newArrayListWithExpectedSize(names.size());
      final List<MapSqlParameterSource> nameBatch = Lists.newArrayListWithExpectedSize(names.size());
      for (QName name : names) {
        final long id = nameIdIncrementer.nextLongValue();
        final String localName = name.getLocalName();
        final URI ns = name.getNamespaceURI();

        nameBatch.add(new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("local_name", localName)
                .addValue("namespace",  ns == null ? null : ns.toString()));

        created.add(new RelationalQName(id, name));
      }
      nameInsert.executeBatch(nameBatch.toArray(new MapSqlParameterSource[nameBatch.size()]));

      for (RelationalQName n : created) {
        foundNames.add(n);
        nameCache.put(n, n.getId());
      }
    }

    return foundNames;
  }

  public QNameSet getSet(QName name) {
    final RelationalQName rn = (RelationalQName) get(name);

    final StringBuilder sql = new StringBuilder("select ");
    sql.append(selectNameFrom("m"));
    sql.append(" from text_qname_set ns");
    sql.append(" join text_qname_set m on ns.member = m.id");
    sql.append(" where ns.name = ?");

    final Set<QName> members = Sets.newHashSet();
    jt.query(sql.toString(), new RowMapper<Void>() {

      public Void mapRow(ResultSet rs, int rowNum) throws SQLException {
        members.add(mapNameFrom(rs, "m"));
        return null;
      }
    }, rn.getId());

    return new SimpleQNameSet(rn, Sets.newTreeSet(members));
  }

  public QNameSet putSet(QName name, Set<QName> members) {
    final RelationalQName rn = (RelationalQName) get(name);
    final List<SqlParameterSource> batch = Lists.newArrayListWithExpectedSize(members.size());
    members = get(members);
    for (QName n : members) {
      batch.add(new MapSqlParameterSource()
              .addValue("name", rn.getId())
              .addValue("member", ((RelationalQName) n).getId()));
    }
    nameSetInsert.executeBatch(batch.toArray(new SqlParameterSource[batch.size()]));
    return new SimpleQNameSet(rn, Sets.newTreeSet(members));
  }

  public void deleteSet(QName name) {
    final RelationalQName rn = (RelationalQName) get(name);
    jt.update("delete from text_qname_set where name = ?", rn.getId());
  }

  public synchronized void clearCache() {
    nameCache = null;
  }

  @Required
  public void setDataSource(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  @Required
  public void setIncrementerFactory(DataFieldMaxValueIncrementerFactory incrementerFactory) {
    this.incrementerFactory = incrementerFactory;
  }

  public void setCacheSize(int cacheSize) {
    this.cacheSize = cacheSize;
  }

  public void afterPropertiesSet() throws Exception {
    this.jt = (dataSource == null ? null : new SimpleJdbcTemplate(dataSource));
    this.nameInsert = new SimpleJdbcInsert(dataSource).withTableName("text_qname");
    this.nameSetInsert = new SimpleJdbcInsert(dataSource).withTableName("text_qname_set");
    this.nameIdIncrementer = this.incrementerFactory.create("text_qname");
  }

  private void initCache() {
    nameCache = new MapMaker().maximumSize(cacheSize).makeMap();
    if (jt.queryForInt("select count(*) from text_qname") <= cacheSize) {
      // warm-up cache
      for (RelationalQName name : jt.query("select " + selectNameFrom("n") + " from text_qname n", ROW_MAPPER)) {
        nameCache.put(name, name.getId());
      }
    }
  }

  public static String selectNameFrom(String tableName) {
    return SQL.select(tableName, "id", "local_name", "namespace");
  }

  public static RelationalQName mapNameFrom(ResultSet rs, String prefix) throws SQLException {
    final RelationalQName name = new RelationalQName();
    name.setId(rs.getInt(prefix + "_id"));
    name.setLocalName(rs.getString(prefix + "_local_name"));
    name.setNamespace(rs.getString(prefix + "_namespace"));
    return name;
  }

  private static final RowMapper<RelationalQName> ROW_MAPPER = new RowMapper<RelationalQName>() {

    public RelationalQName mapRow(ResultSet rs, int rowNum) throws SQLException {
      return mapNameFrom(rs, "n");
    }
  };
}
