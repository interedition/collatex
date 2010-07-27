package eu.interedition.collatex2.experimental.graph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import eu.interedition.collatex2.implementation.CollateXEngine;
import eu.interedition.collatex2.interfaces.IWitness;

public class VariantGraph2Test {
  private static CollateXEngine engine;

  @BeforeClass
  public static void setup() {
    engine = new CollateXEngine();
  }

  @Test
  public void testRepeatingTokensWithOneWitness() {
    final IWitness witness = engine.createWitness("a", "a c a t g c a");
    final IVariantGraph graph = engine.graph(witness);
    final List<String> repeatingTokens = graph.findRepeatingTokens();
    assertEquals(2, repeatingTokens.size());
    assertTrue(repeatingTokens.contains("a"));
    assertTrue(repeatingTokens.contains("c"));
    assertFalse(repeatingTokens.contains("t"));
    assertFalse(repeatingTokens.contains("g"));
  }

}
