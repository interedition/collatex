package eu.interedition.collatex.neo4j;

import static eu.interedition.collatex.neo4j.GraphRelationshipType.VARIANT_GRAPH;
import static org.neo4j.graphdb.Direction.INCOMING;
import static org.neo4j.graphdb.Direction.OUTGOING;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

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
public class GraphFactory {
  private static final Logger LOG = LoggerFactory.getLogger(GraphFactory.class);
  public static final String CREATED_KEY = "created";

  private EntityMapper<Witness> witnessMapper = new SimpleWitnessMapper();
  private EntityMapper<Token> tokenMapper = new SimpleTokenMapper();
  private GraphDatabaseService database;
  private Node variantGraphs;

  public static GraphFactory create(File dbStorageDirectory, final boolean deleteAfterUsage) throws IOException {
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
    
    return new GraphFactory(database);
  }

  public static GraphFactory create(File dbStorageDirectory) throws IOException {
    return create(dbStorageDirectory, false);    
  }

  public static GraphFactory create() throws IOException {
    return create(Files.createTempDir());
  }

  
  public GraphFactory(GraphDatabaseService database, EntityMapper<Witness> witnessMapper, EntityMapper<Token> tokenMapper) {
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

  public GraphFactory(EmbeddedGraphDatabase database) {
    this(database, new SimpleWitnessMapper(), new SimpleTokenMapper());
  }
  
  public GraphDatabaseService getDatabase() {
    return database;
  }

  public Iterable<Neo4jVariantGraph> variantGraphs() {
    return Iterables.transform(variantGraphs.getRelationships(VARIANT_GRAPH, OUTGOING), new Function<Relationship, Neo4jVariantGraph>() {
      @Override
      public Neo4jVariantGraph apply(Relationship input) {
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

  public void delete(Neo4jVariantGraph vg) {
    final Node startNode = vg.getStart().getNode();
    startNode.getSingleRelationship(VARIANT_GRAPH, INCOMING).delete();
    startNode.getSingleRelationship(VARIANT_GRAPH, OUTGOING).delete();
    for (Neo4jVariantGraphVertex v : vg.vertices()) {
      for (Neo4jVariantGraphEdge e : v.incoming()) {
        e.delete();
      }
      for (Neo4jVariantGraphTransposition t : v.transpositions()) {
        t.delete();
      }
      v.delete();
    }
  }
  
  protected Neo4jVariantGraph wrapVariantGraph(Relationship startEndRel) {
    final Node startNode = startEndRel.getEndNode();
    return wrapVariantGraph(startNode, startNode.getSingleRelationship(VARIANT_GRAPH, OUTGOING).getEndNode());
  }

  protected Neo4jVariantGraph wrapVariantGraph(Node start, Node end) {
    final Neo4jVariantGraph graph = new Neo4jVariantGraph(database, witnessMapper, tokenMapper);
    graph.init(Neo4jVariantGraphVertex.createWrapper(graph), Neo4jVariantGraphEdge.createWrapper(graph), start, end);
    return graph;
  }
}
