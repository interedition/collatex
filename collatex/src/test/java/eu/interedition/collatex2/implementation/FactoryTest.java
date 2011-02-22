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

package eu.interedition.collatex2.implementation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import eu.interedition.collatex2.interfaces.IWitness;

public class FactoryTest {
  private static CollateXEngine factory;

  @BeforeClass
  public static void setup() {
    factory = new CollateXEngine();
  }


  @Ignore
  @Test
  public void testGetPhrasesWithMultiples() {
    final IWitness a = factory.createWitness("A", "the big black cat and the big black rat");
    final IWitness b = factory.createWitness("B", "the big black rat and the small white rat");
    final Set<String> tokensWithMultiples = CollateXEngine.getPhrasesWithMultiples(a, b);
    final String[] expectedPhrases = { "the big black", "rat" };
    assertEquals(tokensWithMultiples.toString(), expectedPhrases.length, tokensWithMultiples.size());
    for (final String expected : expectedPhrases) {
      assertContains(tokensWithMultiples, expected);
    }
  }

  private void assertContains(final Set<String> stringSet, final String string) {
    assertTrue(string + " not found in " + stringSet, stringSet.contains(string));
  }

}
