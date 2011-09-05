package eu.interedition.text.graph;

import org.neo4j.graphdb.GraphDatabaseService;

import java.io.File;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public interface GraphDataSource {

  GraphDatabaseService getGraphDatabaseService();

  File getContentStore();
}
