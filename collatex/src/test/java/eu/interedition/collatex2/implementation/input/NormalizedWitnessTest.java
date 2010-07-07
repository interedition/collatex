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
    final List<String> repeatingTokens = witness.findRepeatingTokens();
    assertEquals(2, repeatingTokens.size());
    assertTrue(repeatingTokens.contains("a"));
    assertTrue(repeatingTokens.contains("b"));
    assertFalse(repeatingTokens.contains("d"));
    assertFalse(repeatingTokens.contains("f"));
  }
}
