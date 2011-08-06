package eu.interedition.text.rdbms;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.DatabaseMetaDataCallback;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.MetaDataAccessException;
import org.springframework.jdbc.support.incrementer.DataFieldMaxValueIncrementer;
import org.springframework.jdbc.support.incrementer.H2SequenceMaxValueIncrementer;
import org.springframework.jdbc.support.incrementer.MySQLMaxValueIncrementer;

import javax.sql.DataSource;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class DataFieldMaxValueIncrementerFactory implements InitializingBean {
  private DataSource dataSource;
  private DatabaseType databaseType;

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
    final String dbProductName = (String) JdbcUtils.extractDatabaseMetaData(dataSource, new DatabaseMetaDataCallback() {
      public Object processMetaData(DatabaseMetaData dbmd) throws SQLException, MetaDataAccessException {
        return dbmd.getDatabaseProductName();
      }
    });

    if ("h2".equalsIgnoreCase(dbProductName)) {
      databaseType = DatabaseType.H2;
    } else if ("mysql".equalsIgnoreCase(dbProductName)) {
      databaseType = DatabaseType.MYSQL;
    } else {
      throw new IllegalArgumentException(dataSource.toString());
    }
  }

  private enum DatabaseType {
    H2, MYSQL;
  }
}
