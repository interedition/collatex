package eu.interedition.collatex2.experimental.graph;

import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Test;

import eu.interedition.collatex2.implementation.CollateXEngine;
import eu.interedition.collatex2.interfaces.IWitness;

public class AlignmentGraphIndexTest {
  private static CollateXEngine factory;

  @BeforeClass
  public static void setup() {
    factory = new CollateXEngine();
  }

  @Test
  public void testCreateAlignmentTableIndex() {
    final IWitness a = factory.createWitness("A", "the first witness");
    final IAlignmentGraph graph = factory.graph(a);
    final IAlignmentGraphIndex index = AlignmentGraphIndex.create(graph, graph.findRepeatingTokens());
    assertEquals("AlignmentGraphIndex: (the, first, witness)", index.toString());
  }

//  @Test
//  public void testCreateAlignmentTableIndexWithVariation() {
//    final IWitness a = factory.createWitness("A", "the first witness");
//    final IWitness b = factory.createWitness("B", "the second witness");
//    final IAlignmentTable table = factory.align(a, b);
//    final IAlignmentTableIndex index = AlignmentTableIndex.create(table, table.findRepeatingTokens());
//    assertEquals("AlignmentTableIndex: (the, first, witness, second)", index.toString());
//  }

}
