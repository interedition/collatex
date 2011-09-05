package eu.interedition.text.graph;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.io.Files;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.io.File;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class TemporaryGraphDataSource implements GraphDataSource, InitializingBean, DisposableBean {
    private final File base = Files.createTempDir();

    private EmbeddedGraphDatabase graphDatabaseService;
    private File contentHome;

    @Override
    public GraphDatabaseService getGraphDatabaseService() {
        return graphDatabaseService;
    }

    public File getBase() {
        return base;
    }

    @Override
    public File getContentStore() {
        return contentHome;
    }

    @Override
    public void destroy() throws Exception {
        try {
            graphDatabaseService.shutdown();
        } finally {
            Files.deleteRecursively(base.getCanonicalFile());
        }

    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.graphDatabaseService = new EmbeddedGraphDatabase(new File(base, "graph").getAbsolutePath());

        this.contentHome = new File(base, "content");
        Preconditions.checkState(this.contentHome.mkdir(), "Cannot create content store home");
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).addValue(base).toString();
    }
}
