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

package com.sd_editions.collatex.permutations;

import junit.framework.TestCase;

import org.junit.Test;

import eu.interedition.collatex.input.Witness;
import eu.interedition.collatex.input.builders.WitnessBuilder;

public class MultiMatchMapTest extends TestCase {

  private WitnessBuilder builder;

  @Override
  protected void setUp() throws Exception {
    builder = new WitnessBuilder();
    super.setUp();
  }

  @Test
  public void testDetermineBase() {
    Witness witness1 = builder.build("The Black Cat");
    Witness witness2 = builder.build("The Cat and the Dog");
    Witness witness3 = builder.build("The White Cat");
    MultiMatchMap mmm = new MultiMatchMap(witness1.getFirstSegment(), witness2.getFirstSegment(), witness3.getFirstSegment());
    assertEquals(2, mmm.keySet().size());
    assertTrue(mmm.containsKey("the"));
    assertTrue(mmm.containsKey("cat"));

    MultiMatch theMultiMatch = mmm.get("the");
    // 'the' occurs once in the 1st witness
    assertEquals(1, theMultiMatch.getOccurancesInWitness(witness1.getFirstSegment().id).size());
    // 'the' occurs twice in the 2nd witness
    assertEquals(2, theMultiMatch.getOccurancesInWitness(witness2.getFirstSegment().id).size());
    // 'the' occurs once in the 3rd witness
    assertEquals(1, theMultiMatch.getOccurancesInWitness(witness3.getFirstSegment().id).size());
    assertEquals("the cat", mmm.getNormalizedMatchSentence());
  }

  @Test
  public void testMultiMatchShrinksAtThirdWitness() {
    Witness witness1 = builder.build("The Black Cat");
    Witness witness2 = builder.build("The black dog and white cat");
    Witness witness3 = builder.build("The White Cat");
    MultiMatchMap mmm = new MultiMatchMap(witness1.getFirstSegment(), witness2.getFirstSegment(), witness3.getFirstSegment());
    assertEquals(2, mmm.keySet().size());
    assertTrue(mmm.containsKey("the"));
    assertTrue(mmm.containsKey("cat"));

    MultiMatch theMultiMatch = mmm.get("the");
    assertEquals(1, theMultiMatch.getOccurancesInWitness(witness1.getFirstSegment().id).size());
    assertEquals(1, theMultiMatch.getOccurancesInWitness(witness2.getFirstSegment().id).size());
    assertEquals(1, theMultiMatch.getOccurancesInWitness(witness3.getFirstSegment().id).size());
  }

}
