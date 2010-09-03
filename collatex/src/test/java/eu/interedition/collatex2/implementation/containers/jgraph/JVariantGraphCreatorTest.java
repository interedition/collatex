package eu.interedition.collatex2.implementation.containers.jgraph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;

import eu.interedition.collatex2.implementation.CollateXEngine;
import eu.interedition.collatex2.implementation.containers.graph.VariantGraph2Creator;
import eu.interedition.collatex2.interfaces.IJVariantGraph;
import eu.interedition.collatex2.interfaces.IJVariantGraphEdge;
import eu.interedition.collatex2.interfaces.IJVariantGraphVertex;
import eu.interedition.collatex2.interfaces.IVariantGraph;
import eu.interedition.collatex2.interfaces.IWitness;

public class JVariantGraphCreatorTest {
  private static CollateXEngine engine;

  @BeforeClass
  public static void setup() {
    engine = new CollateXEngine();
  }

  //  @Ignore
  @Test
  public void testJoinTwoIdenticalWitnesses() {
    final IWitness w1 = engine.createWitness("A", "the black cat");
    final IWitness w2 = engine.createWitness("B", "the black cat");
    IVariantGraph graph = VariantGraph2Creator.create(w1, w2);
    IJVariantGraph joinedGraph = JVariantGraphCreator.parallelSegmentate(graph);
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

  }
}
