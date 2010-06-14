package eu.interedition.collatex2.experimental.graph;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import eu.interedition.collatex2.implementation.CollateXEngine;
import eu.interedition.collatex2.interfaces.IWitness;

public class VariantGraphSpencerHoweTest {
  private static CollateXEngine engine;

  @BeforeClass
  public static void setup() {
    engine = new CollateXEngine();
  }

  //Note: this only tests the Graph, not the table!
  @Test
  public void testSimpleSpencerHowe() {
    IWitness w1 = engine.createWitness("A", "a");
    IWitness w2 = engine.createWitness("B", "b");
    IWitness w3 = engine.createWitness("C", "a b");
    VariantGraph graph = VariantGraph.create();
    graph.addWitness(w1);
    graph.addWitness(w2);
    graph.addWitness(w3);
    assertEquals(4, graph.getVertices().size());
    final List<IVariantGraphEdge> edges = graph.getEdges();
    assertEquals(5, edges.size());
    assertEquals("# -> a: A, C", edges.get(0).toString());
    assertEquals("# -> b: B", edges.get(1).toString());
    assertEquals("a -> #: A", edges.get(2).toString());
    assertEquals("a -> b: C", edges.get(3).toString());
    assertEquals("b -> #: B, C", edges.get(4).toString());
  }
  



}
