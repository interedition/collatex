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

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.incrementer.DataFieldMaxValueIncrementer;
import org.springframework.jdbc.support.incrementer.H2SequenceMaxValueIncrementer;
import org.springframework.jdbc.support.incrementer.MySQLMaxValueIncrementer;

import javax.sql.DataSource;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class RelationalDatabaseKeyFactory implements InitializingBean {
  private DataSource dataSource;
  private RelationalDatabaseType databaseType;

  @Required
  public void setDataSource(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  public DataFieldMaxValueIncrementer create(String id) {
    switch (databaseType) {
      case H2:
        return new H2SequenceMaxValueIncrementer(dataSource, id + "_sequence");
      case MYSQL:
        return init(new MySQLMaxValueIncrementer(dataSource, "text_incrementer", id + "_sequence"));
      default:
        throw new IllegalStateException();
    }
  }

  private DataFieldMaxValueIncrementer init(MySQLMaxValueIncrementer incrementer) {
    final JdbcTemplate jt = new JdbcTemplate(dataSource);
    final String tableName = incrementer.getIncrementerName();
    if (jt.queryForInt("select count(*) from " + tableName) == 0) {
      jt.update("insert into " + tableName + " values ()");
    }

    incrementer.setCacheSize(Short.MAX_VALUE);

    return incrementer;
  }

  public void afterPropertiesSet() throws Exception {
    databaseType = RelationalDatabaseType.detect(dataSource);
  }

}
