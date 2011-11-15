package eu.interedition.text;

import eu.interedition.text.rdbms.RelationalDatabaseType;
import eu.interedition.text.rdbms.RelationalTextRepository;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

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

    final ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
    populator.setContinueOnError(true);
    populator.addScript(RelationalDatabaseType.detect(ds).getSchemaResource());

    final DataSourceInitializer initializer = new DataSourceInitializer();
    initializer.setDataSource(ds);
    initializer.setDatabasePopulator(populator);
    initializer.afterPropertiesSet();

    return ds;
  }
}
