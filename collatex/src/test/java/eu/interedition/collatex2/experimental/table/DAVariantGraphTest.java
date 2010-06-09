package eu.interedition.collatex2.experimental.table;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

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
    VariantGraph graph = VariantGraph.create();
    graph.addWitness(w1);
    graph.addWitness(w2);
    graph.addWitness(w3);
    assertEquals(3, graph.getVertices().size());
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

}
