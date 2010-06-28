package eu.interedition.collatex2.experimental.table;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import eu.interedition.collatex2.experimental.graph.IVariantGraph;
import eu.interedition.collatex2.experimental.graph.VariantGraph;
import eu.interedition.collatex2.implementation.CollateXEngine;
import eu.interedition.collatex2.interfaces.IWitness;

public class DAVariantGraphTest {
  private static CollateXEngine engine;

  @BeforeClass
  public static void setup() {
    engine = new CollateXEngine();
  }

  @Test
  public void testLongestPath() {
    IWitness w1 = engine.createWitness("A", "a");
    IWitness w2 = engine.createWitness("B", "b");
    IWitness w3 = engine.createWitness("C", "a b");
    IVariantGraph graph = VariantGraph.create();
    graph.addWitness(w1);
    graph.addWitness(w2);
    graph.addWitness(w3);
    assertEquals(4, graph.getVertices().size());
    DAGBuilder builder = new DAGBuilder();
    DAVariantGraph avg = builder.buildDAG(graph);
    List<CollateXVertex> longestPath = avg.getLongestPath();
//    for (CollateXVertex v: longestPath) {
//      System.out.println(v.getNormalized());
//    }
    assertEquals("a", longestPath.get(0).getNormalized());
    assertEquals("b", longestPath.get(1).getNormalized());
    assertEquals(2, longestPath.size());
  }

  @Test
  public void testGetPathForWitness() {
    final IWitness w1 = engine.createWitness("V", "a b c d e f ");
    final IWitness w2 = engine.createWitness("W", "x y z d e");
    final IWitness w3 = engine.createWitness("X", "a b x y z");
    IVariantGraph graph = VariantGraph.create();
    graph.addWitness(w1);
    graph.addWitness(w2);
    graph.addWitness(w3);
    DAGBuilder builder = new DAGBuilder();
    DAVariantGraph avg = builder.buildDAG(graph);
    List<CollateXVertex> path = avg.getPathFor(w1);
    assertEquals("a", path.get(0).getNormalized());
    assertEquals("b", path.get(1).getNormalized());
    assertEquals("c", path.get(2).getNormalized());
    assertEquals("d", path.get(3).getNormalized());
    assertEquals("e", path.get(4).getNormalized());
    assertEquals("f", path.get(5).getNormalized());
    assertEquals(6, path.size());
  }
}
