package eu.interedition.collatex.implementation.graph.db;

import com.google.common.collect.Sets;
import com.google.common.io.Files;
import eu.interedition.collatex.interfaces.Token;
import eu.interedition.collatex.interfaces.IWitness;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

import static eu.interedition.collatex.implementation.graph.db.VariantGraphRelationshipType.START_END;
import static eu.interedition.collatex.implementation.graph.db.VariantGraphRelationshipType.VARIANT_GRAPH;
import static org.neo4j.graphdb.Direction.OUTGOING;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class VariantGraphFactory {
  private static final Logger LOG = LoggerFactory.getLogger(VariantGraphFactory.class);

  private Resolver<IWitness> witnessResolver = new DefaultResolver<IWitness>();
  private Resolver<Token> tokenResolver = new DefaultResolver<Token>();
  private EmbeddedGraphDatabase db;
  private Node variantGraphs;

  public VariantGraphFactory() throws IOException {
    this(Files.createTempDir(), true);
  }

  public VariantGraphFactory(File dbStorageDirectory) throws IOException {
    this(dbStorageDirectory, false);
  }

  public VariantGraphFactory(File dbStorageDirectory, final boolean deleteAfterUsage) throws IOException {
    final File dbDirectory = dbStorageDirectory.getCanonicalFile();

    LOG.debug("Creating variant graph database in {} (deleteAfterUsage = {})", dbDirectory, deleteAfterUsage);
    db = new EmbeddedGraphDatabase(dbDirectory.getAbsolutePath());
    Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
      @Override
      public void run() {
        db.shutdown();
        if (deleteAfterUsage && dbDirectory.isDirectory()) {
          try {
            Files.deleteRecursively(dbDirectory);
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      }
    }));

    final Transaction tx = db.beginTx();
    try {
      final Node referenceNode = db.getReferenceNode();
      final Relationship r = referenceNode.getSingleRelationship(VARIANT_GRAPH, OUTGOING);
      if (r == null) {
        referenceNode.createRelationshipTo(variantGraphs = db.createNode(), VARIANT_GRAPH);
      } else {
        variantGraphs = r.getEndNode();
      }
      tx.success();
    } finally {
      tx.finish();
    }
  }

  public EmbeddedGraphDatabase getDb() {
    return db;
  }

  public void setWitnessResolver(Resolver<IWitness> witnessResolver) {
    this.witnessResolver = witnessResolver;
  }

  public void setTokenResolver(Resolver<Token> tokenResolver) {
    this.tokenResolver = tokenResolver;
  }

  public synchronized VariantGraph create() {
    final Transaction tx = db.beginTx();
    try {
      final Node start = db.createNode();
      final Node end = db.createNode();

      variantGraphs.createRelationshipTo(start, START_END);
      end.createRelationshipTo(variantGraphs, START_END);

      final VariantGraph graph = new VariantGraph(start, end, witnessResolver, tokenResolver);
      graph.getStart().setTokens(Sets.<Token>newTreeSet());
      graph.getEnd().setTokens(Sets.<Token>newTreeSet());
      graph.connect(graph.getStart(), graph.getEnd(), Sets.<IWitness>newTreeSet());

      tx.success();
      return graph;
    } finally {
      tx.finish();
    }
  }
}
