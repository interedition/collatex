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

//Make this part of the same package so we can test the protected methods
package com.sd_editions.collatex.Block;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for Word Word.
 */
public class WordTest extends TestCase {
  /**
   * Create the test case
   *
   * @param testName name of the test case
   */
  public WordTest(String testName) {
    super(testName);
  }

  /**
   * @return the suite of tests being tested
   */
  public static Test suite() {
    return new TestSuite(WordTest.class);
  }

  /**
   * Simple check that we can set and get text content
   */
  public void testCreateWord() {
    String textContent = "some content";
    Word tword = new Word(textContent);
    assertNotNull(tword);

    assertEquals(tword.getContent(), textContent);
  }

  /**
   * Simple check of setting/getting attributes and things
   */
  public void testWordAttributes() {
    String textContent = "text content";
    Word tword = new Word(textContent);

    String attrName1 = "aname1";
    String attrValue1 = "avalue1";
    String attrName2 = "aname2";
    String attrValue2 = "avalue2";

    //Set the attributes
    tword.setAttribute(attrName1, attrValue1);
    tword.setAttribute(attrName2, attrValue2);

    //We do have two attributes don't we?
    assertEquals(tword.numberOfAttributes(), 2);

    //Check that they are right
    assertEquals(tword.getAttribute(attrName1), attrValue1);
    assertEquals(tword.getAttribute(attrName2), attrValue2);

    //Try removing one
    tword.removeAttribute(attrName1);
    assertEquals(tword.numberOfAttributes(), 1);

    //Try removing the last
    tword.removeAttribute(attrName2);
    assertEquals(tword.numberOfAttributes(), 0);
  }
}
