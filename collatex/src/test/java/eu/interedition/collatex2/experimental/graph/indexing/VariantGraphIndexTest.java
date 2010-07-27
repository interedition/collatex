package eu.interedition.collatex2.experimental.graph.indexing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import eu.interedition.collatex2.experimental.graph.IVariantGraph;
import eu.interedition.collatex2.implementation.CollateXEngine;
import eu.interedition.collatex2.interfaces.IWitness;

public class VariantGraphIndexTest {
  private static CollateXEngine factory;

  @BeforeClass
  public static void setup() {
    factory = new CollateXEngine();
  }

  @Test
  public void testCreateAlignmentTableIndex() {
    final IWitness a = factory.createWitness("A", "the first witness");
    final IVariantGraph graph = factory.graph(a);
    final IVariantGraphIndex index = VariantGraphIndex.create(graph, graph.findRepeatingTokens());
    assertEquals("AlignmentGraphIndex: (the, first, witness)", index.toString());
  }
  
  @Ignore
  @Test
  public void test1() {
    final IWitness a = factory.createWitness("A", "the big black cat and the big black rat");
    final IVariantGraph graph = factory.graph(a);
    final IVariantGraphIndex index = VariantGraphIndex.create(graph, graph.findRepeatingTokens());
    //TODO: Note: duplicated down below!
    assertTrue(index.contains("cat"));
    assertTrue(index.contains("and"));
    assertTrue(index.contains("rat"));
    assertEquals(15, index.size());
    assertTrue(index.contains("# the"));
    assertTrue(index.contains("# the big"));
    assertTrue(index.contains("# the big black"));
    assertTrue(index.contains("the big black cat"));
    assertTrue(index.contains("big black cat"));
    assertTrue(index.contains("black cat"));
    assertTrue(index.contains("cat"));
    assertTrue(index.contains("and"));
    assertTrue(index.contains("and the"));
    assertTrue(index.contains("and the big"));
    assertTrue(index.contains("and the big black"));
    assertTrue(index.contains("the big black rat"));
    assertTrue(index.contains("big black rat"));
    assertTrue(index.contains("black rat"));
    assertTrue(index.contains("rat"));
    assertEquals(15, index.size());
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
