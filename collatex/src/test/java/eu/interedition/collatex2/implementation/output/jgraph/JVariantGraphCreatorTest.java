package eu.interedition.collatex2.implementation.output.jgraph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import eu.interedition.collatex2.implementation.CollateXEngine;
import eu.interedition.collatex2.implementation.containers.graph.VariantGraph2Creator;
import eu.interedition.collatex2.implementation.output.jgraph.JVariantGraphCreator;
import eu.interedition.collatex2.interfaces.IJVariantGraph;
import eu.interedition.collatex2.interfaces.IJVariantGraphEdge;
import eu.interedition.collatex2.interfaces.IJVariantGraphVertex;
import eu.interedition.collatex2.interfaces.IVariantGraph;
import eu.interedition.collatex2.interfaces.IWitness;

public class JVariantGraphCreatorTest {
  private static final Logger LOG = LoggerFactory.getLogger(JVariantGraphCreatorTest.class);
  private static CollateXEngine engine;

  @BeforeClass
  public static void setup() {
    engine = new CollateXEngine();
  }

  @Test
  public void testJoinTwoIdenticalWitnesses() {
    final IWitness w1 = engine.createWitness("A", "the black cat");
    final IWitness w2 = engine.createWitness("B", "the black cat");
    IVariantGraph graph = VariantGraph2Creator.create(w1, w2);
    IJVariantGraph joinedGraph = JVariantGraphCreator.parallelSegmentate(graph);
    LOG.info("joinedGraph=" + joinedGraph);
    IJVariantGraphVertex startVertex = joinedGraph.getStartVertex();
    assertEquals("#", startVertex.getNormalized());
    assertEquals(0, startVertex.getWitnesses().size());

    Set<IJVariantGraphEdge> outgoingEdges = joinedGraph.outgoingEdgesOf(startVertex);
    assertEquals(1, outgoingEdges.size());

    IJVariantGraphEdge edge = outgoingEdges.iterator().next();
    IJVariantGraphVertex vertex = joinedGraph.getEdgeTarget(edge);
    assertEquals("the black cat", vertex.getNormalized());

    Set<IWitness> witnesses = edge.getWitnesses();
    assertEquals(2, witnesses.size());
    assertTrue(witnesses.contains(w1));
    assertTrue(witnesses.contains(w2));

    List<String> phrases = extractPhrases(joinedGraph);
    assertEquals(phrases.toString(), 3, phrases.size());
    assertTrue(phrases.contains("#"));
    assertTrue(phrases.contains("the black cat"));
  }

  @Test
  public void testJoinTwoDifferentWitnesses() {
    final IWitness w1 = engine.createWitness("A", "the nice black cat shared his food");
    final IWitness w2 = engine.createWitness("B", "the bad white cat spilled his food again");
    IVariantGraph graph = VariantGraph2Creator.create(w1, w2);
    IJVariantGraph joinedGraph = JVariantGraphCreator.parallelSegmentate(graph);
    LOG.info("joinedGraph=" + joinedGraph);
    IJVariantGraphVertex startVertex = joinedGraph.getStartVertex();
    assertEquals("#", startVertex.getNormalized());
    assertEquals(0, startVertex.getWitnesses().size());

    Set<IJVariantGraphEdge> outgoingEdges = joinedGraph.outgoingEdgesOf(startVertex);
    assertEquals(1, outgoingEdges.size());

    IJVariantGraphEdge edge = outgoingEdges.iterator().next();
    IJVariantGraphVertex vertex = joinedGraph.getEdgeTarget(edge);
    assertEquals("the", vertex.getNormalized());

    Set<IWitness> witnesses = edge.getWitnesses();
    assertEquals(2, witnesses.size());
    assertTrue(witnesses.contains(w1));
    assertTrue(witnesses.contains(w2));

    List<String> phrases = extractPhrases(joinedGraph);
    assertEquals(phrases.toString(), 10, phrases.size());
    assertTrue(phrases.contains("#"));
    assertTrue(phrases.contains("the"));
    assertTrue(phrases.contains("nice black"));
    assertTrue(phrases.contains("bad white"));
    assertTrue(phrases.contains("cat"));
    assertTrue(phrases.contains("shared"));
    assertTrue(phrases.contains("spilled"));
    assertTrue(phrases.contains("his food"));
    assertTrue(phrases.contains("again"));
  }

  @Test
  public void testJoinTwoDifferentWitnesses2() {
    final IWitness w1 = engine.createWitness("A", "Blackie, the black cat");
    final IWitness w2 = engine.createWitness("B", "Whitney, the white cat");
    IVariantGraph graph = VariantGraph2Creator.create(w1, w2);
    IJVariantGraph joinedGraph = JVariantGraphCreator.parallelSegmentate(graph);
    LOG.info("joinedGraph=" + joinedGraph);
    IJVariantGraphVertex startVertex = joinedGraph.getStartVertex();
    assertEquals("#", startVertex.getNormalized());
    assertEquals(0, startVertex.getWitnesses().size());

    Set<IJVariantGraphEdge> outgoingEdges = joinedGraph.outgoingEdgesOf(startVertex);
    assertEquals(2, outgoingEdges.size());

    Iterator<IJVariantGraphEdge> iterator = outgoingEdges.iterator();
    IJVariantGraphEdge edge = iterator.next();
    IJVariantGraphVertex vertex = joinedGraph.getEdgeTarget(edge);
    assertEquals("blackie", vertex.getNormalized());

    edge = iterator.next();
    vertex = joinedGraph.getEdgeTarget(edge);
    assertEquals("whitney", vertex.getNormalized());

    Set<IWitness> witnesses = edge.getWitnesses();
    assertEquals(1, witnesses.size());
    assertTrue(witnesses.contains(w2));

    List<String> phrases = extractPhrases(joinedGraph);
    assertEquals(phrases.toString(), 8, phrases.size());
    assertTrue(phrases.contains("#"));
    assertTrue(phrases.contains("blackie"));
    assertTrue(phrases.contains("whitney"));
    assertTrue(phrases.contains("the"));
    assertTrue(phrases.contains("black"));
    assertTrue(phrases.contains("white"));
    assertTrue(phrases.contains("cat"));
  }

  private List<String> extractPhrases(IJVariantGraph joinedGraph) {
    Set<IJVariantGraphVertex> vertexSet = joinedGraph.vertexSet();
    List<String> phrases = Lists.newArrayList();
    for (IJVariantGraphVertex variantGraphVertex : vertexSet) {
      phrases.add(variantGraphVertex.getNormalized());
    }
    return phrases;
  }
}
