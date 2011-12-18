package eu.interedition.collatex.implementation.graph;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import eu.interedition.collatex.implementation.input.SimpleToken;
import eu.interedition.collatex.interfaces.IWitness;
import eu.interedition.collatex.interfaces.Token;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

import static eu.interedition.collatex.implementation.graph.GraphRelationshipType.*;
import static org.neo4j.graphdb.Direction.INCOMING;
import static org.neo4j.graphdb.Direction.OUTGOING;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class GraphFactory {
  private static final Logger LOG = LoggerFactory.getLogger(GraphFactory.class);
  public static final String CREATED_KEY = "created";

  private Resolver<IWitness> witnessResolver = new DefaultResolver<IWitness>();
  private Resolver<Token> tokenResolver = new DefaultResolver<Token>();
  private EmbeddedGraphDatabase database;
  private Node variantGraphs;
  private Node editGraphs;

  public GraphFactory() throws IOException {
    this(Files.createTempDir(), true);
  }

  public GraphFactory(File dbStorageDirectory) throws IOException {
    this(dbStorageDirectory, false);
  }

  public GraphFactory(File dbStorageDirectory, final boolean deleteAfterUsage) throws IOException {
    final File dbDirectory = dbStorageDirectory.getCanonicalFile();

    LOG.debug("Creating variant graph database in {} (deleteAfterUsage = {})", dbDirectory, deleteAfterUsage);
    database = new EmbeddedGraphDatabase(dbDirectory.getAbsolutePath());
    Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
      @Override
      public void run() {
        database.shutdown();
        if (deleteAfterUsage && dbDirectory.isDirectory()) {
          try {
            Files.deleteRecursively(dbDirectory);
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      }
    }));

    final Transaction tx = database.beginTx();
    try {
      final Node referenceNode = database.getReferenceNode();
      final Relationship vgRel = referenceNode.getSingleRelationship(VARIANT_GRAPH, OUTGOING);
      if (vgRel == null) {
        referenceNode.createRelationshipTo(variantGraphs = database.createNode(), VARIANT_GRAPH);
      } else {
        variantGraphs = vgRel.getEndNode();
      }

      final Relationship egRel = referenceNode.getSingleRelationship(EDIT_GRAPH, OUTGOING);
      if (egRel == null) {
        referenceNode.createRelationshipTo(editGraphs = database.createNode(), VARIANT_GRAPH);
      } else {
        editGraphs = egRel.getEndNode();
      }
      tx.success();
    } finally {
      tx.finish();
    }
  }

  public EmbeddedGraphDatabase getDatabase() {
    return database;
  }

  public void setWitnessResolver(Resolver<IWitness> witnessResolver) {
    this.witnessResolver = witnessResolver;
  }

  public void setTokenResolver(Resolver<Token> tokenResolver) {
    this.tokenResolver = tokenResolver;
  }

  public Iterable<VariantGraph> variantGraphs() {
    return Iterables.transform(variantGraphs.getRelationships(VARIANT_GRAPH, OUTGOING), new Function<Relationship, VariantGraph>() {
      @Override
      public VariantGraph apply(Relationship input) {
        return wrapVariantGraph(input);
      }
    });
  }
  
  public Iterable<EditGraph> editGraphs() {
    return Iterables.transform(editGraphs.getRelationships(EDIT_GRAPH, OUTGOING), new Function<Relationship, EditGraph>() {
      @Override
      public EditGraph apply(Relationship input) {
        return wrapEditGraph(input);
      }
    });
  }
  
  public VariantGraph newVariantGraph() {
    final Node startNode = database.createNode();
    final Node endNode = database.createNode();

    final Relationship startRel = variantGraphs.createRelationshipTo(startNode, VARIANT_GRAPH);
    startRel.setProperty(CREATED_KEY, System.currentTimeMillis());
    startNode.createRelationshipTo(endNode, VARIANT_GRAPH);

    final VariantGraph graph = wrapVariantGraph(startNode, endNode);
    final VariantGraphVertex start = graph.getStart();
    final VariantGraphVertex end = graph.getEnd();

    start.setTokens(Sets.<Token>newTreeSet());
    end.setTokens(Sets.<Token>newTreeSet());
    graph.connect(start, end, Sets.<IWitness>newTreeSet());

    return graph;
  }

  public EditGraph newEditGraph(VariantGraph vg) {
    final Node startNode = database.createNode();
    final Node endNode = database.createNode();

    final Relationship startRel = editGraphs.createRelationshipTo(startNode, EDIT_GRAPH);
    startRel.setProperty(CREATED_KEY, System.currentTimeMillis());
    
    startNode.createRelationshipTo(endNode, EDIT_GRAPH);
    startNode.createRelationshipTo(vg.getStart().getNode(), VARIANT_GRAPH);

    final EditGraph graph = wrapEditGraph(startNode, endNode);
    final EditGraphVertex start = graph.getStart();
    final EditGraphVertex end = graph.getEnd();

    start.setBase(vg.getStart());
    start.setWitness(SimpleToken.START);
    start.setWitnessIndex(-1);

    end.setBase(vg.getEnd());
    end.setWitness(SimpleToken.END);
    end.setWitnessIndex(Integer.MAX_VALUE);

    return graph;
  }

  public void deleteGraphsOlderThan(long timestamp) {
    for (Relationship vgRel : variantGraphs.getRelationships(VARIANT_GRAPH, OUTGOING)) {
      if (((Long) vgRel.getProperty(CREATED_KEY)) < timestamp) {
        delete(wrapVariantGraph(vgRel));
      }
    }
    for (Relationship egRel : editGraphs.getRelationships(EDIT_GRAPH, OUTGOING)) {
      if (((Long) egRel.getProperty(CREATED_KEY)) < timestamp) {
        delete(wrapEditGraph(egRel));
      }
    }
  }

  public void delete(VariantGraph vg) {
    final Node startNode = vg.getStart().getNode();
    for (Relationship rel : startNode.getRelationships(VARIANT_GRAPH, INCOMING)) {
      final Node relStart = rel.getStartNode();
      if (!relStart.equals(variantGraphs)) {
        delete(wrapEditGraph(relStart, relStart.getSingleRelationship(EDIT_GRAPH, OUTGOING).getEndNode()));
      }
    }

    startNode.getSingleRelationship(VARIANT_GRAPH, INCOMING).delete();
    startNode.getSingleRelationship(VARIANT_GRAPH, OUTGOING).delete();
    for (VariantGraphVertex v : vg.vertices()) {
      for (VariantGraphEdge e : v.incoming()) {
        e.delete();
      }
      for (VariantGraphTransposition t : v.transpositions()) {
        t.delete();
      }
      v.delete();
    }
  }
  
  public void delete(EditGraph eg) {
    final Node startNode = eg.getStart().getNode();
    startNode.getSingleRelationship(VARIANT_GRAPH, OUTGOING).delete();
    startNode.getSingleRelationship(EDIT_GRAPH, INCOMING).delete();
    startNode.getSingleRelationship(EDIT_GRAPH, OUTGOING).delete();

    for (EditGraphVertex v : eg.vertices()) {
      for (EditGraphEdge e : v.incoming()) {
        e.delete();
      }
      v.delete();
    }
  }

  protected VariantGraph wrapVariantGraph(Relationship startEndRel) {
    final Node startNode = startEndRel.getEndNode();
    return wrapVariantGraph(startNode, startNode.getSingleRelationship(VARIANT_GRAPH, OUTGOING).getEndNode());
  }

  protected VariantGraph wrapVariantGraph(Node start, Node end) {
    final VariantGraph graph = new VariantGraph(database, witnessResolver, tokenResolver);
    graph.init(VariantGraphVertex.createWrapper(graph), VariantGraphEdge.createWrapper(graph), start, end);
    return graph;
  }

  protected EditGraph wrapEditGraph(Relationship startEndRel) {
    final Node startNode = startEndRel.getEndNode();
    return wrapEditGraph(startNode, startNode.getSingleRelationship(EDIT_GRAPH, OUTGOING).getEndNode());
  }

  protected EditGraph wrapEditGraph(Node start, Node end) {
    final VariantGraph vg = wrapVariantGraph(start.getSingleRelationship(VARIANT_GRAPH, OUTGOING));
    final EditGraph graph = new EditGraph(database, witnessResolver, tokenResolver, vg.getVertexWrapper());
    graph.init(EditGraphVertex.createWrapper(graph), EditGraphEdge.createWrapper(graph), start, end);
    return graph;
  }

}
