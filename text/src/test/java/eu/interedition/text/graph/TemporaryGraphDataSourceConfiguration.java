package eu.interedition.text.graph;

import com.google.common.io.Files;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.kernel.impl.transaction.SpringTransactionManager;
import org.neo4j.kernel.impl.transaction.UserTransactionImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.jta.JtaTransactionManager;

import java.io.File;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@Configuration
public class TemporaryGraphDataSourceConfiguration {
    protected static final Logger LOG = LoggerFactory.getLogger(TemporaryGraphDataSourceConfiguration.class.getPackage().getName());

    @Bean
    public TemporaryGraphDataSource graphDataSource() {
        final TemporaryGraphDataSource ds = new TemporaryGraphDataSource();
        if (LOG.isInfoEnabled()) {
            LOG.info("Created temporary graph data source in {}", ds.getBase());
        }
        return ds;
    }

    @Bean
    public PlatformTransactionManager transactionManager() {
        final GraphDatabaseService db = graphDataSource().getGraphDatabaseService();

        final JtaTransactionManager transactionManager = new JtaTransactionManager();
        transactionManager.setTransactionManager(new SpringTransactionManager(db));
        transactionManager.setUserTransaction(new UserTransactionImpl(db));
        return transactionManager;
    }
}
