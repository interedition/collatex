package eu.interedition.text.rdbms;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.support.DatabaseMetaDataCallback;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.MetaDataAccessException;

import javax.sql.DataSource;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

/**
* @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
*/
public enum RelationalDatabaseType {
  H2, MYSQL;

  public static RelationalDatabaseType detect(DataSource dataSource) throws MetaDataAccessException {
    final String dbProductName = (String) JdbcUtils.extractDatabaseMetaData(dataSource, new DatabaseMetaDataCallback() {
          public Object processMetaData(DatabaseMetaData dbmd) throws SQLException, MetaDataAccessException {
            return dbmd.getDatabaseProductName();
          }
        });

    if ("h2".equalsIgnoreCase(dbProductName)) {
      return H2;
    } else if ("mysql".equalsIgnoreCase(dbProductName)) {
      return MYSQL;
    } else {
      throw new IllegalArgumentException(dbProductName);
    }
  }

  public Resource getSchemaResource() {
    switch (this) {
      case H2:
        return new ClassPathResource("h2-schema.sql", getClass());
      case MYSQL:
        return new ClassPathResource("mysql-schema.sql", getClass());
    }
    throw new IllegalStateException();
  }
}
