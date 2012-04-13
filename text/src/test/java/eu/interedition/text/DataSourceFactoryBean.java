package eu.interedition.text;

import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import javax.sql.DataSource;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class DataSourceFactoryBean extends AbstractFactoryBean<DataSource> {
  @Override
  public Class<?> getObjectType() {
    return DataSource.class;
  }

  @Override
  protected DataSource createInstance() throws Exception {
    DataSource ds = null;
    String dbUrl = System.getProperty("interedition.text.db.url");
    if (dbUrl != null) {
      String dbUser = System.getProperty("interedition.text.db.user", "root");
      String dbPassword = System.getProperty("interedition.text.db.password", "root");
      ds = new DriverManagerDataSource(dbUrl, dbUser, dbPassword);
    } else {
      ds = new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.H2).build();
    }
    return ds;
  }
}
