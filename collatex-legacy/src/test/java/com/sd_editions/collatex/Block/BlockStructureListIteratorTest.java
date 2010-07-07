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

import java.util.NoSuchElementException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for BlockStructureListIterator
 */
public class BlockStructureListIteratorTest extends TestCase {
  /**
   * Create the test case
   *
   * @param testName name of the test case
   */
  public BlockStructureListIteratorTest(String testName) {
    super(testName);
  }

  /**
   * @return the suite of tests being tested
   */
  public static Test suite() {
    return new TestSuite(BlockStructureListIteratorTest.class);
  }

  @SuppressWarnings("null")
  public void testIteratorBasic() {
    //Create a basic BlockStructure
    Line line = new Line(1);
    Word word1 = new Word("The");
    Word word2 = new Word("cat");
    Word word3 = new Word("sat");
    Word word4 = new Word("on");
    Word word5 = new Word("the");
    Word word6 = new Word("mat");
    BlockStructure document = null;
    try {
      document = new BlockStructure();
      document.setRootBlock(line);
    } catch (BlockStructureCascadeException e) {
      fail("Could not create block structure");
    }
    document.setChildBlock(line, word1);
    document.setNextSibling(word1, word2);
    document.setNextSibling(word2, word3);
    document.setNextSibling(word3, word4);
    document.setNextSibling(word4, word5);
    document.setNextSibling(word5, word6);

    BlockStructureListIterator<?> it = document.listIterator();
    //We should be able to get an iterator
    assertNotNull(it);
    //There should be a next element
    assertTrue(it.hasNext());
    //But no previous element
    assertFalse(it.hasPrevious());
    //First get line
    assertEquals(line, it.next());
    assertTrue(it.hasNext());
    assertFalse(line + " should not have a previous element", it.hasPrevious());
    //Next should be word1
    assertEquals(word1, it.next());
    assertTrue(it.hasNext());
    assertTrue(it.hasPrevious());
    //Next should be word2
    assertEquals(word2, it.next());
    assertTrue(it.hasNext());
    assertTrue(it.hasPrevious());
    //Next should be word3
    assertEquals(word3, it.next());
    assertTrue(it.hasNext());
    assertTrue(it.hasPrevious());
    //Go back one, should get word2
    assertEquals(word2, it.previous());
    assertTrue(it.hasNext());
    assertTrue(it.hasPrevious());
    //Go back one, should get word1
    assertEquals(word1, it.previous());
    assertTrue(it.hasNext());
    assertTrue(it.hasPrevious());
    //Go back one, should get line
    assertEquals(line, it.previous());
    assertTrue(it.hasNext());
    assertFalse(it.hasPrevious());
    //Next should be word1
    assertEquals(word1, it.next());
    assertTrue(it.hasNext());
    assertTrue(it.hasPrevious());
    //Next should be word2
    assertEquals(word2, it.next());
    assertTrue(it.hasNext());
    assertTrue(it.hasPrevious());
    //Next should be word3
    assertEquals(word3, it.next());
    assertTrue(it.hasNext());
    assertTrue(it.hasPrevious());
    //Next should be word4
    assertEquals(word4, it.next());
    assertTrue(it.hasNext());
    assertTrue(it.hasPrevious());
    //Next should be word5
    assertEquals(word5, it.next());
    assertTrue(it.hasNext());
    assertTrue(it.hasPrevious());
    //Next should be word6
    assertEquals(word6, it.next());
    assertFalse(it.hasNext());
    assertTrue(it.hasPrevious());
    //We're at the end so it should throw an exception
    try {
      it.next();
      fail();
    } catch (NoSuchElementException e) {}
  }
}
