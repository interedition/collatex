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

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.MapMaker;
import com.google.common.collect.Sets;
import eu.interedition.text.QName;
import eu.interedition.text.QNameRepository;
import eu.interedition.text.util.SQL;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.jdbc.support.incrementer.DataFieldMaxValueIncrementer;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.net.URI;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static java.util.Collections.singleton;

public class RelationalQNameRepository implements QNameRepository, InitializingBean {

  private DataSource dataSource;
  private PlatformTransactionManager transactionManager;
  private DataFieldMaxValueIncrementerFactory incrementerFactory;
  private int cacheSize = 1000;

  private SimpleJdbcTemplate jt;
  private SimpleJdbcInsert nameInsert;
  private DataFieldMaxValueIncrementer nameIdIncrementer;

  private Map<QName, Long> nameCache;
  private TransactionTemplate tt;

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

    final Set<QName> requested = Sets.newHashSet(names);
    final Set<QName> found = Sets.newHashSetWithExpectedSize(requested.size());

    for (Iterator<QName> it = requested.iterator(); it.hasNext(); ) {
      final QName name = it.next();
      final Long id = nameCache.get(name);
      if (id != null) {
        found.add(new RelationalQName(id, name));
        it.remove();
      }
    }

    if (requested.isEmpty()) {
      return found;
    }

    return tt.execute(new TransactionCallback<Set<QName>>() {
      @Override
      public Set<QName> doInTransaction(TransactionStatus status) {
        final List<Object> ps = Lists.newArrayList();
        final StringBuilder sql = new StringBuilder("select ").append(selectNameFrom("n")).append(" from text_qname n where ");
        for (Iterator<QName> it = requested.iterator(); it.hasNext(); ) {
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
          found.add(name);
          requested.remove(name);
          nameCache.put(name, name.getId());
        }

        final List<RelationalQName> created = Lists.newArrayListWithExpectedSize(requested.size());
        final List<MapSqlParameterSource> nameBatch = Lists.newArrayListWithExpectedSize(requested.size());
        for (QName name : requested) {
          final long id = nameIdIncrementer.nextLongValue();
          final String localName = name.getLocalName();
          final URI ns = name.getNamespaceURI();

          nameBatch.add(new MapSqlParameterSource()
                  .addValue("id", id)
                  .addValue("local_name", localName)
                  .addValue("namespace", ns == null ? null : ns.toString()));

          created.add(new RelationalQName(id, name));
        }
        nameInsert.executeBatch(nameBatch.toArray(new MapSqlParameterSource[nameBatch.size()]));

        for (RelationalQName n : created) {
          found.add(n);
          nameCache.put(n, n.getId());
        }

        return found;
      }
    });
  }

  public synchronized void clearCache() {
    nameCache = null;
  }

  @Required
  public void setDataSource(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  @Required
  public void setTransactionManager(PlatformTransactionManager transactionManager) {
    this.transactionManager = transactionManager;
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
    this.nameIdIncrementer = this.incrementerFactory.create("text_qname");

    this.tt = new TransactionTemplate(transactionManager);
    this.tt.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
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
