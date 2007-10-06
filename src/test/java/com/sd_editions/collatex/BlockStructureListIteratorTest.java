//Make this part of the same package so we can test the protected methods
package com.sd_editions.collatex.Block;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for BlockStructureListIterator
 */
public class BlockStructureListIteratorTest extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public BlockStructureListIteratorTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
	  return new TestSuite( BlockStructureListIteratorTest.class );
	}

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

	  BlockStructureListIterator it = document.listIterator();
	  //We should be able to get an iterator
	  assertNotNull(it);
	  //There should be a next element
	  assertTrue(it.hasNext());
	  //But no previous element
	  assertFalse(it.hasPrevious());
	  //First get line
	  assertEquals(line, it.next());
	  assertTrue(it.hasNext());
	  assertFalse(it.hasPrevious());
	  //Next should be word1
	  assertEquals(word1, it.next());
	  assertTrue(it.hasNext());
	  assertTrue(it.hasPrevious());
	}
}


