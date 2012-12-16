package eu.interedition.collatex.neo4j;

import static eu.interedition.collatex.neo4j.Neo4jGraphRelationships.VARIANT_GRAPH;
import static org.neo4j.graphdb.Direction.INCOMING;
import static org.neo4j.graphdb.Direction.OUTGOING;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

import eu.interedition.collatex.VariantGraph;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.io.Files;

import eu.interedition.collatex.Token;
import eu.interedition.collatex.Witness;
import eu.interedition.collatex.simple.SimpleTokenMapper;
import eu.interedition.collatex.simple.SimpleWitnessMapper;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class Neo4jVariantGraphFactory {
  private static final Logger LOG = LoggerFactory.getLogger(Neo4jVariantGraphFactory.class);
  public static final String CREATED_KEY = "created";

  private EntityMapper<Witness> witnessMapper = new SimpleWitnessMapper();
  private EntityMapper<Token> tokenMapper = new SimpleTokenMapper();
  private GraphDatabaseService database;
  private Node variantGraphs;

  public static Neo4jVariantGraphFactory create(File dbStorageDirectory, final boolean deleteAfterUsage) throws IOException {
    final File dbDirectory = dbStorageDirectory.getCanonicalFile();

    LOG.debug("Creating variant graph database in {} (deleteAfterUsage = {})", dbDirectory, deleteAfterUsage);
    final EmbeddedGraphDatabase database = new EmbeddedGraphDatabase(dbDirectory.getAbsolutePath());
    Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
      @Override
      public void run() {
        database.shutdown();
        if (deleteAfterUsage && dbDirectory.isDirectory()) {
            delete(dbDirectory);
        }
      }

      void delete(File directory) {
        if (directory.isDirectory()) {
          for (File file : directory.listFiles()) {
            if (file.isDirectory()) {
              delete(file);
            }
            file.delete();
          }
        }
        directory.delete();
      }
    }));
    
    return new Neo4jVariantGraphFactory(database);
  }

  public static Neo4jVariantGraphFactory create(File dbStorageDirectory) throws IOException {
    return create(dbStorageDirectory, false);    
  }

  public static Neo4jVariantGraphFactory create() throws IOException {
    return create(Files.createTempDir());
  }

  
  public Neo4jVariantGraphFactory(GraphDatabaseService database, EntityMapper<Witness> witnessMapper, EntityMapper<Token> tokenMapper) {
    this.database = database;
    this.witnessMapper = witnessMapper;
    this.tokenMapper = tokenMapper;
    final Transaction tx = database.beginTx();
    try {
      final Node referenceNode = database.getReferenceNode();
      final Relationship vgRel = referenceNode.getSingleRelationship(VARIANT_GRAPH, OUTGOING);
      if (vgRel == null) {
        referenceNode.createRelationshipTo(variantGraphs = database.createNode(), VARIANT_GRAPH);
      } else {
        variantGraphs = vgRel.getEndNode();
      }
      tx.success();
    } finally {
      tx.finish();
    }
  }

  public Neo4jVariantGraphFactory(EmbeddedGraphDatabase database) {
    this(database, new SimpleWitnessMapper(), new SimpleTokenMapper());
  }
  
  public GraphDatabaseService getDatabase() {
    return database;
  }

  public Iterable<VariantGraph> variantGraphs() {
    return Iterables.transform(variantGraphs.getRelationships(VARIANT_GRAPH, OUTGOING), new Function<Relationship, VariantGraph>() {
      @Override
      public VariantGraph apply(Relationship input) {
        return wrapVariantGraph(input);
      }
    });
  }
  
  public Neo4jVariantGraph newVariantGraph() {
    final Node startNode = database.createNode();
    final Node endNode = database.createNode();

    final Relationship startRel = variantGraphs.createRelationshipTo(startNode, VARIANT_GRAPH);
    startRel.setProperty(CREATED_KEY, System.currentTimeMillis());
    startNode.createRelationshipTo(endNode, VARIANT_GRAPH);

    final Neo4jVariantGraph graph = wrapVariantGraph(startNode, endNode);
    final Neo4jVariantGraphVertex start = graph.getStart();
    final Neo4jVariantGraphVertex end = graph.getEnd();

    start.setTokens(Collections.<Token>emptySet());
    end.setTokens(Collections.<Token>emptySet());
    graph.connect(start, end, Collections.<Witness>emptySet());

    return graph;
  }

  
  public void deleteGraphsOlderThan(long timestamp) {
    for (Relationship vgRel : variantGraphs.getRelationships(VARIANT_GRAPH, OUTGOING)) {
      if (((Long) vgRel.getProperty(CREATED_KEY)) < timestamp) {
        delete(wrapVariantGraph(vgRel));
      }
    }
  }

  public void delete(VariantGraph vg) {
    final Node startNode = vg.getStart().getNode();
    startNode.getSingleRelationship(VARIANT_GRAPH, INCOMING).delete();
    startNode.getSingleRelationship(VARIANT_GRAPH, OUTGOING).delete();
    for (VariantGraph.Vertex v : vg.vertices()) {
      for (VariantGraph.Edge e : v.incoming()) {
        e.delete();
      }
      for (VariantGraph.Transposition t : v.transpositions()) {
        t.delete();
      }
      v.delete();
    }
  }

  protected VariantGraph wrapVariantGraph(Relationship startEndRel) {
    final Node startNode = startEndRel.getEndNode();
    return wrapVariantGraph(startNode, startNode.getSingleRelationship(VARIANT_GRAPH, OUTGOING).getEndNode());
  }

  protected Neo4jVariantGraph wrapVariantGraph(Node start, Node end) {
    return new Neo4jVariantGraph(database, start, end, witnessMapper, tokenMapper);
  }
}
