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

package eu.interedition.collatex2.usecases.dirk;

import eu.interedition.collatex2.implementation.CollateXEngine;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IVariantGraph;
import eu.interedition.collatex2.interfaces.IWitness;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.*;

public class HighFrequencyWordsTest {
  private static CollateXEngine factory;
  
  @BeforeClass
  public static void setup() {
    factory = new CollateXEngine();
  }
  
  @Test
  public void testNodeIsNear() {
    IWitness witness = factory.createWitness("A", "a b c d e f g h i j k l");
    List<INormalizedToken> tokens = witness.getTokens();
    Iterator<INormalizedToken> iterator = tokens.iterator();
    INormalizedToken a = iterator.next();
    INormalizedToken b = iterator.next();
    INormalizedToken c = iterator.next();
    INormalizedToken d = iterator.next();
    assertTrue(witness.isNear(a, b));
    assertFalse(witness.isNear(a, c));
    assertFalse(witness.isNear(b, d));
    assertTrue(witness.isNear(c, d));
  }
  
  @Test
  public void testGraphGetTokens() {
    IWitness witnessA = factory.createWitness("A", "a b c d");
    IVariantGraph graph = factory.graph(witnessA);
    List<INormalizedToken> tokensA = graph.getTokens(witnessA);
    assertEquals("a", tokensA.get(0).getNormalized());
    //TODO: etc
  }
}
