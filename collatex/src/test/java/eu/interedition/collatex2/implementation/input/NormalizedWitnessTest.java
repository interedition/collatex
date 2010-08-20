package eu.interedition.collatex2.implementation.input;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import eu.interedition.collatex2.implementation.CollateXEngine;
import eu.interedition.collatex2.interfaces.IWitness;

public class NormalizedWitnessTest {
  @Test
  public void test() {
    final IWitness witness = new CollateXEngine().createWitness("a", "a b a d b f a");
    final List<String> repeatingTokens = witness.getRepeatedTokens();
    assertEquals(2, repeatingTokens.size());
    assertTrue(repeatingTokens.contains("a"));
    assertTrue(repeatingTokens.contains("b"));
    assertFalse(repeatingTokens.contains("d"));
    assertFalse(repeatingTokens.contains("f"));
  }
}
