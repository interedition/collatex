package eu.interedition.collatex;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.ProvisionException;
import com.google.inject.name.Named;
import eu.interedition.collatex.neo4j.GraphFactory;
import eu.interedition.collatex.simple.SimpleTokenMapper;
import eu.interedition.collatex.simple.SimpleWitnessMapper;
import org.neo4j.kernel.EmbeddedGraphDatabase;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class GraphFactoryProvider implements Provider<GraphFactory> {
  private Logger LOG = Logger.getLogger(getClass().getName());
  private final File graphDirectory;

  @Inject
  public GraphFactoryProvider(@Named("interedition.data") String dataDirectory) {
    this.graphDirectory = new File(dataDirectory, "graphs");
  }

  public GraphFactory get() {
    try {
      if (LOG.isLoggable(Level.INFO)) {
        LOG.info("Starting graph database in " + graphDirectory);
      }

      final EmbeddedGraphDatabase graphDatabase = new EmbeddedGraphDatabase(graphDirectory.getCanonicalPath());
      Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
        @Override
        public void run() {
          if (LOG.isLoggable(Level.INFO)) {
            LOG.info("Stopping graph database in " + graphDirectory);
          }
          graphDatabase.shutdown();
        }
      }));

      return new GraphFactory(graphDatabase, new SimpleWitnessMapper(), new SimpleTokenMapper());
    } catch (IOException e) {
      throw new ProvisionException("I/O error while starting graph database in " + graphDirectory, e);
    }
  }

}
