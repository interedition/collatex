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

package com.sd_editions.collatex.match;

import junit.framework.TestCase;

public class WordMatchesTest extends TestCase {

  private WordMatches testWordMatches;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    String testWord = "testWord";
    testWordMatches = new WordMatches(testWord);
  }

  public final void testGetWord() {
    String word = "woord";
    WordMatches wm = new WordMatches(word);
    assertEquals(word, wm.getWord());
  }

  public final void testAddExactMatch() {
    assertEquals(0, testWordMatches.getExactMatches().size());
    WordCoordinate matchCoordinate = new WordCoordinate(1, 2);
    testWordMatches.addExactMatch(matchCoordinate);
    assertEquals(1, testWordMatches.getExactMatches().size());
    assertEquals(matchCoordinate, testWordMatches.getExactMatches().toArray()[0]);
  }

  public final void testAddLevMatch() {
    assertEquals(0, testWordMatches.getLevMatches().size());
    WordCoordinate matchCoordinate1 = new WordCoordinate(2, 4);
    WordCoordinate matchCoordinate2 = new WordCoordinate(3, 14);
    testWordMatches.addLevMatch(matchCoordinate1);
    testWordMatches.addLevMatch(matchCoordinate2);
    assertEquals(2, testWordMatches.getLevMatches().size());
    assertEquals(matchCoordinate1, testWordMatches.getLevMatches().toArray()[0]);
    assertEquals(matchCoordinate2, testWordMatches.getLevMatches().toArray()[1]);
  }

  public void testToString() throws Exception {
    testWordMatches.addExactMatch(new WordCoordinate(1, 2));
    testWordMatches.addExactMatch(new WordCoordinate(3, 4));
    testWordMatches.addLevMatch(new WordCoordinate(1, 3));
    testWordMatches.addLevMatch(new WordCoordinate(2, 4));
    assertEquals("testWord: exact=[[B,3], [D,5]], lev=[[B,4], [C,5]]", testWordMatches.toString());
  }

  public void testGetPermutations() throws Exception {
    testWordMatches.addExactMatch(new WordCoordinate(1, 2));
    testWordMatches.addExactMatch(new WordCoordinate(1, 3));
    testWordMatches.addExactMatch(new WordCoordinate(2, 0));
    testWordMatches.addExactMatch(new WordCoordinate(2, 4));
    testWordMatches.addExactMatch(new WordCoordinate(3, 4));
    String expected = "[testWord: exact=[[B,3], [C,1], [D,5]], lev=[], testWord: exact=[[B,3], [C,5], [D,5]], lev=[], testWord: exact=[[B,4], [C,1], [D,5]], lev=[], testWord: exact=[[B,4], [C,5], [D,5]], lev=[]]";
    assertEquals(expected, testWordMatches.getPermutations().toString());
  }
}
