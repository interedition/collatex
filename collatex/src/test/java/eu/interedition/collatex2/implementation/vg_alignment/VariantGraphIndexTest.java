/**
 * CollateX - a Java library for collating textual sources,
 * for example, to produce an apparatus.
 *
 * Copyright (C) 2010 ESF COST Action "Interedition".
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.interedition.collatex2.implementation.vg_alignment;

import eu.interedition.collatex2.implementation.CollateXEngine;
import eu.interedition.collatex2.interfaces.IVariantGraph;
import eu.interedition.collatex2.interfaces.IWitness;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class VariantGraphIndexTest {
  private static CollateXEngine factory;

  @BeforeClass
  public static void setup() {
    factory = new CollateXEngine();
  }
  
  @Test
  public void testRepeatingTokensWithOneWitness() {
    final IWitness witness = factory.createWitness("a", "a c a t g c a");
    final IVariantGraph graph = factory.graph(witness);
    final List<String> repeatingTokens = TokenIndexUtil.getRepeatedTokens(graph);
    assertEquals(2, repeatingTokens.size());
    assertTrue(repeatingTokens.contains("a"));
    assertTrue(repeatingTokens.contains("c"));
    assertFalse(repeatingTokens.contains("t"));
    assertFalse(repeatingTokens.contains("g"));
  }

  @Test
  public void testRepeatingTokensWithMultipleWitnesses() {
    final IWitness witnessA = factory.createWitness("a", "a c a t g c a");
    final IWitness witnessB = factory.createWitness("b", "a c a t t c a");
    final IVariantGraph graph = factory.graph(witnessA, witnessB);
    final List<String> repeatingTokens = TokenIndexUtil.getRepeatedTokens(graph);
    assertEquals(3, repeatingTokens.size());
    assertTrue(repeatingTokens.contains("a"));
    assertTrue(repeatingTokens.contains("c"));
    assertTrue(repeatingTokens.contains("t"));
    assertFalse(repeatingTokens.contains("g"));
  }

  @Test
  public void testRepeatingTokensWithMultipleWitnesses2() {
    final IWitness witnessA = factory.createWitness("A", "everything is unique should be no problem");
    final IWitness witnessB = factory.createWitness("B", "this one very different");
    final IVariantGraph graph = factory.graph(witnessA, witnessB);
    final List<String> repeatingTokens = TokenIndexUtil.getRepeatedTokens(graph);
    assertEquals(0, repeatingTokens.size());
  }

  @Test
  public void testGetRepeatedTokensWithMultipleWitnesses() {
    final IWitness a = factory.createWitness("A", "the big black cat and the big black rat");
    final IWitness b = factory.createWitness("B", "the big black rat and the small white rat");
    IVariantGraph graph = factory.graph(a, b);
    final List<String> repeatedTokens = TokenIndexUtil.getRepeatedTokens(graph);
    final String[] expectedTokens = { "the", "big", "black", "rat" };
    assertEquals(expectedTokens.length, repeatedTokens.size());
    for (final String expected : expectedTokens) {
      assertTrue(repeatedTokens.contains(expected));
    }
  }
}
