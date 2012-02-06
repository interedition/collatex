package eu.interedition.web.collatex;

import eu.interedition.collatex.graph.GraphFactory;
import org.neo4j.graphdb.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@Service
public class VariantGraphCleanup implements Runnable {
  private static final Logger LOG = LoggerFactory.getLogger(VariantGraphCleanup.class);
  private static final int TWO_HOURS = 7200000;

  @Autowired
  private GraphFactory graphFactory;

  @Override
  public void run() {
    final Transaction tx = graphFactory.getDatabase().beginTx();
    try {
      LOG.debug("Purging graphs older than 2 hours");
      graphFactory.deleteGraphsOlderThan(System.currentTimeMillis() - TWO_HOURS);
      tx.success();
    } finally {
      tx.finish();
    }
  }
}
