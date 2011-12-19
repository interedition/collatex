package eu.interedition.web;

import com.google.common.collect.Sets;
import com.google.common.io.Files;
import com.jolbox.bonecp.BoneCPDataSource;
import eu.interedition.collatex.implementation.graph.GraphFactory;
import eu.interedition.text.json.map.TextSerializerModule;
import eu.interedition.web.io.RangeConverter;
import eu.interedition.web.text.TextIndex;
import org.codehaus.jackson.map.ObjectMapper;
import org.h2.Driver;
import org.neo4j.graphdb.GraphDatabaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.format.support.FormattingConversionServiceFactoryBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.Assert;
import org.springframework.web.util.UriUtils;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@Configuration
@ComponentScan(
        basePackageClasses = ApplicationConfiguration.class,
        includeFilters = {@ComponentScan.Filter(Service.class)},
        useDefaultFilters = false)
@ImportResource("classpath:/eu/interedition/text/rdbms/repository-context.xml")
public class ApplicationConfiguration {
  private static final String DATA_DIRECTORY = System.getProperty("interedition.data");
  private static final Logger LOG = LoggerFactory.getLogger(ApplicationConfiguration.class);

  @Bean
  public File dataDirectory() throws IOException {
    if (DATA_DIRECTORY == null) {
      final File temporaryDataDirectory = Files.createTempDir().getCanonicalFile();

      LOG.info("Creating temporary data directory '{}'", temporaryDataDirectory);
      Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
        @Override
        public void run() {
          try {
            Files.deleteRecursively(temporaryDataDirectory);
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      }));

      return temporaryDataDirectory;
    } else {
      final File dataDirectory = new File(DATA_DIRECTORY);
      Assert.isTrue(dataDirectory.isDirectory() || dataDirectory.mkdirs(),//
              "Data directory '" + dataDirectory + "' does not exist and could not be created");
      return dataDirectory;
    }
  }

  @Bean
  public GraphFactory graphFactory() throws IOException {
    return new GraphFactory(new File(dataDirectory(), "graphs"));
  }

  @Bean
  public GraphDatabaseService graphDatabase() throws IOException {
    return graphFactory().getDatabase();
  }

  @Bean(name = { "dataSource", "repositoryDataSource" }, destroyMethod = "close")
  public DataSource relationalDataSource() throws IOException {
    final File dbHome = new File(dataDirectory(), "relations");
    Assert.isTrue(dbHome.isDirectory() || dbHome.mkdirs(),//
            "RDBMS directory '" + dbHome + "' does not exist and could not be created");

    final BoneCPDataSource dataSource = new BoneCPDataSource();
    dataSource.setDriverClass(Driver.class.getName());
    dataSource.setJdbcUrl("jdbc:h2://" + UriUtils.encodePath(new File(dbHome, "h2").getAbsolutePath(), "UTF-8"));
    dataSource.setUsername("sa");
    dataSource.setPassword("");
    dataSource.setMinConnectionsPerPartition(1);
    dataSource.setMaxConnectionsPerPartition(20);

    final ResourceDatabasePopulator dp = new ResourceDatabasePopulator();
    dp.addScript(new ClassPathResource("/eu/interedition/text/rdbms/h2-schema.sql"));
    dp.addScript(new ClassPathResource("/h2-schema.sql"));
    DatabasePopulatorUtils.execute(dp, dataSource);

    return dataSource;
  }

  @Bean(name = { "transactionManager", "repositoryTransactionManager" })
  public PlatformTransactionManager transactionManager() throws IOException {
    return new DataSourceTransactionManager(relationalDataSource());
  }

  @Bean
  public JdbcTemplate jdbcTemplate() throws IOException {
    return new JdbcTemplate(relationalDataSource());
  }

  @Bean
  public TextIndex textIndex() throws IOException {
    final File indexHome = new File(dataDirectory(), "index");
    Assert.isTrue(indexHome.isDirectory() || indexHome.mkdirs(),//
            "Fulltext index directory '" + indexHome + "' does not exist and could not be created");

    return new TextIndex(indexHome);
  }
  
  @Bean
  public FormattingConversionServiceFactoryBean conversionService() {
    final FormattingConversionServiceFactoryBean cs = new FormattingConversionServiceFactoryBean();
    cs.setConverters(Sets.newHashSet(new RangeConverter()));
    return cs;
  }

  @Bean
  public ObjectMapper objectMapper() {
    final ObjectMapper om = new ObjectMapper();
    om.registerModule(new TextSerializerModule());
    return om;
  }

  @Bean
  public ScheduledExecutorService taskScheduler() {
    return Executors.newScheduledThreadPool(42);
  }
}
