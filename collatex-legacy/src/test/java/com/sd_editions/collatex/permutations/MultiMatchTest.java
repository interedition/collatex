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

import java.util.List;

import junit.framework.TestCase;

import org.junit.Test;

import eu.interedition.collatex.input.Word;

public class MultiMatchTest extends TestCase {
  private static final String witnessId1 = "A";
  private static final String witnessId2 = "B";

  @Test
  public void testMultiMatch() {
    /*
     * A: Hello, Hello
     * B: Hello oops
     */
    Word hello1in1 = new Word(witnessId1, "Hello,", 1);
    Word hello2in1 = new Word(witnessId1, "Hello", 2);
    Word hello1in2 = new Word(witnessId2, "Hello", 1);
    Word oops1in1 = new Word(witnessId2, "oops", 2);
    MultiMatch mm = new MultiMatch(hello1in1, hello1in2);
    mm.addMatchingWord(hello2in1);
    mm.addMatchingWord(oops1in1);
    assertEquals("hello", mm.name);
    List<Word> helloIn1 = mm.getOccurancesInWitness(witnessId1);
    List<Word> helloIn2 = mm.getOccurancesInWitness(witnessId2);
    assertEquals(2, helloIn1.size());
    assertEquals(hello1in1, helloIn1.get(0));
    assertEquals(hello2in1, helloIn1.get(1));
    assertEquals(1, helloIn2.size());
    assertEquals(hello1in2, helloIn2.get(0));
  }
}
