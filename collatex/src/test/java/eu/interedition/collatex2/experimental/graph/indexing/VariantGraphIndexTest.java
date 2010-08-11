package eu.interedition.collatex2.experimental.graph.indexing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;

import eu.interedition.collatex2.experimental.graph.IVariantGraph;
import eu.interedition.collatex2.implementation.CollateXEngine;
import eu.interedition.collatex2.interfaces.IWitness;
import eu.interedition.collatex2.interfaces.IWitnessIndex;

public class VariantGraphIndexTest {
  private static CollateXEngine factory;

  @BeforeClass
  public static void setup() {
    factory = new CollateXEngine();
  }

  @Test
  public void testCreateVariantGraphIndex() {
    final IWitness a = factory.createWitness("A", "the first witness");
    final IVariantGraph graph = factory.graph(a);
    final IVariantGraphIndex index = VariantGraphIndex.create(graph, graph.findRepeatingTokens());
    assertEquals("VariantGraphIndex: (the, first, witness)", index.toString());
  }
  

  
  @Test
  public void test1() {
    final IWitness a = factory.createWitness("A", "the big black cat and the big black rat");
    final IVariantGraph graph = factory.graph(a);
    final IVariantGraphIndex index = VariantGraphIndex.create(graph, graph.findRepeatingTokens());
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


  @Test
  public void testCreateVariantGraphIndexWithVariation() {
    final IWitness a = factory.createWitness("A", "the first witness");
    final IWitness b = factory.createWitness("B", "the second witness");
    final IVariantGraph graph = factory.graph(a, b);
    final IVariantGraphIndex index = VariantGraphIndex.create(graph, graph.findRepeatingTokens());
    assertEquals("VariantGraphIndex: (the, first, witness, second)", index.toString());
  }
  
  @Test
  public void testVariantGraphIndex() {
    final IWitness a = factory.createWitness("A", "first");
    final IWitness b = factory.createWitness("B", "second");
    final IWitness c = factory.createWitness("C", "third");
    final IVariantGraph graph = factory.graph(a, b, c);
    final IVariantGraphIndex index = VariantGraphIndex.create(graph, graph.findRepeatingTokens());
    assertEquals("VariantGraphIndex: (first, second, third)", index.toString());
  }


  @Test
  public void testVariantGraphIndex2() {
    final IWitness a = factory.createWitness("A", "first");
    final IWitness b = factory.createWitness("B", "match");
    final IWitness c = factory.createWitness("C", "match");
    final IVariantGraph graph = factory.graph(a, b, c);
    final IWitnessIndex index = VariantGraphIndex.create(graph, graph.findRepeatingTokens());
    assertEquals("VariantGraphIndex: (first, match)", index.toString());
  }


}
