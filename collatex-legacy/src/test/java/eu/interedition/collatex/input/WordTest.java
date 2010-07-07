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

package eu.interedition.collatex.input;

import junit.framework.TestCase;

public class WordTest extends TestCase {
  private final static String witnessId = "A";

  public void testNormalize1() {
    Word word = new Word(witnessId, "Hello,", 1);
    assertEquals("hello", word._normalized);
  }

  public void testNormalize2() {
    Word word = new Word(witnessId, "ειπων", 2);
    assertEquals("ειπων", word._normalized);
  }

  public void testEmpty() {
    try {
      new Word(witnessId, "", 3);
      fail();
    } catch (IllegalArgumentException iae) {
      assertEquals(iae.getMessage(), "Word cannot be empty!");
    }
  }
}
