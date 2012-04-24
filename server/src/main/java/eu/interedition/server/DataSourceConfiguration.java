package eu.interedition.server;

import com.jolbox.bonecp.BoneCPDataSource;
import eu.interedition.collatex.graph.GraphFactory;
import eu.interedition.collatex.simple.SimpleTokenMapper;
import eu.interedition.collatex.simple.SimpleWitnessMapper;
import eu.interedition.server.transaction.ChainedTransactionManager;
import eu.interedition.text.Annotation;
import eu.interedition.text.Name;
import eu.interedition.text.Text;
import eu.interedition.text.TextTarget;
import org.h2.Driver;
import org.hibernate.SessionFactory;
import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.neo4j.kernel.impl.transaction.SpringTransactionManager;
import org.neo4j.kernel.impl.transaction.UserTransactionImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.orm.hibernate4.HibernateTransactionManager;
import org.springframework.orm.hibernate4.LocalSessionFactoryBuilder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.jta.JtaTransactionManager;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@Configuration
public class DataSourceConfiguration {

  @Autowired
  private Environment environment;

  @Bean
  public DataSource dataSource() throws Exception {
    final File database = new File(dataDirectory(), "relations");

    final BoneCPDataSource dataSource = new BoneCPDataSource();
    dataSource.setDriverClass(Driver.class.getName());
    dataSource.setJdbcUrl(database.toURI().toString().replaceAll("^file:", "jdbc:h2://"));
    dataSource.setUsername("sa");
    dataSource.setPassword("");
    dataSource.setMinConnectionsPerPartition(1);
    dataSource.setMaxConnectionsPerPartition(20);
    return dataSource;
  }

  @Bean
  public SessionFactory sessionFactory() throws Exception {
    return new LocalSessionFactoryBuilder(dataSource())
            .addAnnotatedClasses(Annotation.class, Name.class, Text.class, TextTarget.class)
            .buildSessionFactory();
  }

  @Bean
  public PlatformTransactionManager transactionManager() throws Exception {
    final EmbeddedGraphDatabase graphDatabase = graphDatabase();
    return new ChainedTransactionManager(
            new JtaTransactionManager(new UserTransactionImpl(graphDatabase), new SpringTransactionManager(graphDatabase)),
            new HibernateTransactionManager(sessionFactory()));
  }

  @Bean
  public GraphFactory graphFactory() throws IOException {
    return new GraphFactory(graphDatabase(), new SimpleWitnessMapper(), new SimpleTokenMapper());
  }

  @Bean(destroyMethod = "shutdown")
  public EmbeddedGraphDatabase graphDatabase() throws IOException {
    return new EmbeddedGraphDatabase(new File(dataDirectory(), "graphs").getCanonicalPath());
  }


  protected File dataDirectory() {
    return environment.getRequiredProperty("interedition.data", File.class);
  }
}
