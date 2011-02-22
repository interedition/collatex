package eu.interedition.collatex2.implementation.containers.graph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import eu.interedition.collatex2.implementation.CollateXEngine;
import eu.interedition.collatex2.interfaces.ITokenIndex;
import eu.interedition.collatex2.interfaces.IVariantGraph;
import eu.interedition.collatex2.interfaces.IWitness;

public class VariantGraphIndexTest {
  private static CollateXEngine factory;

  @BeforeClass
  public static void setup() {
    factory = new CollateXEngine();
  }
  
  @Test
  public void testGetRepeatedTokensWithMultipleWitnesses() {
    final IWitness a = factory.createWitness("A", "the big black cat and the big black rat");
    final IWitness b = factory.createWitness("B", "the big black rat and the small white rat");
    IVariantGraph graph = factory.graph(a, b);
    final List<String> repeatedTokens = graph.getRepeatedTokens();
    final String[] expectedTokens = { "the", "big", "black", "rat" };
    assertEquals(expectedTokens.length, repeatedTokens.size());
    for (final String expected : expectedTokens) {
      assertTrue(repeatedTokens.contains(expected));
    }
  }


  @Test
  public void testCreateVariantGraphIndex() {
    final IWitness a = factory.createWitness("A", "the first witness");
    final IVariantGraph graph = factory.graph(a);
    final ITokenIndex index = new VariantGraphIndex(graph, graph.getRepeatedTokens());
    assertEquals("VariantGraphIndex: (the, first, witness)", index.toString());
  }
  

  
  @Test
  public void test1() {
    final IWitness a = factory.createWitness("A", "the big black cat and the big black rat");
    final IVariantGraph graph = factory.graph(a);
    final ITokenIndex index = new VariantGraphIndex(graph, graph.getRepeatedTokens());
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
    final ITokenIndex index = new VariantGraphIndex(graph, graph.getRepeatedTokens());
    assertEquals("VariantGraphIndex: (the, first, witness, second)", index.toString());
  }
  
  @Test
  public void testVariantGraphIndex() {
    final IWitness a = factory.createWitness("A", "first");
    final IWitness b = factory.createWitness("B", "second");
    final IWitness c = factory.createWitness("C", "third");
    final IVariantGraph graph = factory.graph(a, b, c);
    final ITokenIndex index = new VariantGraphIndex(graph, graph.getRepeatedTokens());
    assertEquals("VariantGraphIndex: (first, second, third)", index.toString());
  }


  @Test
  public void testVariantGraphIndex2() {
    final IWitness a = factory.createWitness("A", "first");
    final IWitness b = factory.createWitness("B", "match");
    final IWitness c = factory.createWitness("C", "match");
    final IVariantGraph graph = factory.graph(a, b, c);
    final ITokenIndex index = new VariantGraphIndex(graph, graph.getRepeatedTokens());
    assertEquals("VariantGraphIndex: (first, match)", index.toString());
  }

  @Test
  public void testIndexWithTwoWitnesses() {
    final IWitness a = factory.createWitness("A", "the big black cat and the big black rat");
    final IWitness b = factory.createWitness("B", "the big black rat and the small white rat");
    IVariantGraph graph = factory.graph(a, b);
    ITokenIndex index = new VariantGraphIndex(graph, graph.getRepeatedTokens());
    assertEquals("VariantGraphIndex: (# the, the big black cat, # the big, big black cat, # the big black, black cat, cat, and, and the, the big black rat #, and the big, big black rat #, and the big black, black rat #, and the big black rat, rat #, the big black rat and, big black rat and, black rat and, # the big black rat, rat and, the small, small, white, white rat)", index.toString());
  }


}
