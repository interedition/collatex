//Make this part of the same package so we can test the protected methods
package com.sd_editions.collatex.Block;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for BlockStructure and its containing Blocks.
 */
public class BlockStructureTest extends TestCase {
  /**
   * Create the test case
   *
   * @param testName name of the test case
   */
  public BlockStructureTest(String testName) {
    super(testName);
  }

  /**
   * @return the suite of tests being tested
   */
  public static Test suite() {
    return new TestSuite(BlockStructureTest.class);
  }

  public void testBlockRelationships() {
    String firstContent = "First Word";
    String twoContent = "Second Word";
    String threeContent = "Third Word";
    String fourContent = "Fourth Word";
    String fifthContent = "Fifth Word";
    //    String sixthContent = "Sixth Content";

    Word word1 = new Word(firstContent);
    Word word2 = new Word(twoContent);
    Word word3 = new Word(threeContent);
    Word word4 = new Word(fourContent);
    Word word5 = new Word(fifthContent);
    //    Word word6 = new Word(sixthContent);

    //Create our BlockStructure to hold the words
    BlockStructure document = new BlockStructure();
    assertEquals(document.getNumberOfBlocks(), 0);
    try {
      document.setRootBlock(word1);
    } catch (BlockStructureCascadeException e) {
      fail("Could not set root block to word1." + e);
    }
    //Make sure we have only 1 block in the BlockStructureCascadeException
    assertEquals(document.getNumberOfBlocks(), 1);
    //Word 1 shouldn't have any
    this.checkBlockHasNoRelations(word1);
    //Word 2 shouldn't have any
    this.checkBlockHasNoRelations(word2);
    //Word 3 shouldn't have any
    this.checkBlockHasNoRelations(word3);
    //Set word1 to the root element
    document.setChildBlock(word1, word2);
    assertEquals(document.getNumberOfBlocks(), 2);
    //Make sure that word1 actually has word2 as it first and last child
    assertTrue(word1.hasFirstChild());
    assertTrue(word1.hasLastChild());
    assertFalse(word1.hasStartParent());
    assertFalse(word1.hasPreviousSibling());
    assertFalse(word1.hasNextSibling());
    assertFalse(word1.hasEndParent());
    assertEquals(word1.getFirstChild(), word2);
    assertEquals(word1.getLastChild(), word2);
    //Word 1 shouldn't have any previous/next siblings
    //Check that word2 has word1 as its start and end ancestor
    assertTrue(word2.hasStartParent());
    assertTrue(word2.hasEndParent());
    assertFalse(word2.hasPreviousSibling());
    assertFalse(word2.hasNextSibling());
    assertEquals(word2.getStartParent(), word1);
    assertEquals(word2.getEndParent(), word1);

    //Now add word3 as a child of word1, it should be added as a next sibling of word2
    document.setChildBlock(word1, word3);
    assertEquals(document.getNumberOfBlocks(), 3);
    //Let's check word1 again, shouldn't have previous/next siblings or ancestors
    assertTrue(word1.hasFirstChild());
    assertTrue(word1.hasLastChild());
    assertFalse(word1.hasStartParent());
    assertFalse(word1.hasEndParent());
    assertFalse(word1.hasPreviousSibling());
    assertFalse(word1.hasNextSibling());
    //The first child should be word2
    assertEquals(word1.getFirstChild(), word2);
    //The last child should be word3
    assertEquals(word1.getLastChild(), word3);
    //Check word 2
    assertTrue(word2.hasStartParent());
    assertTrue(word2.hasEndParent());
    assertFalse(word2.hasFirstChild());
    assertFalse(word2.hasLastChild());
    assertFalse(word2.hasPreviousSibling());
    assertTrue(word2.hasNextSibling());
    assertEquals(word2.getStartParent(), word1);
    assertEquals(word2.getEndParent(), word1);
    assertEquals(word2.getNextSibling(), word3);
    //Check word 3
    assertTrue(word3.hasStartParent());
    assertTrue(word3.hasEndParent());
    assertFalse(word3.hasFirstChild());
    assertFalse(word3.hasLastChild());
    assertTrue(word3.hasPreviousSibling());
    assertFalse(word3.hasNextSibling());
    assertEquals(word3.getStartParent(), word1);
    assertEquals(word3.getEndParent(), word1);
    assertEquals(word3.getPreviousSibling(), word2);

    //Now lets remove word2, which should leave word3 as the only child of word
    try {
      document.removeBlock(word2);
    } catch (BlockStructureCascadeException e) {
      fail("Could not remove word2 from word1: " + e);
    }
    assertEquals(document.getNumberOfBlocks(), 2);
    assertTrue(word1.hasFirstChild());
    assertTrue(word1.hasLastChild());
    assertFalse(word1.hasStartParent());
    assertFalse(word1.hasEndParent());
    assertFalse(word1.hasPreviousSibling());
    assertFalse(word1.hasNextSibling());
    assertEquals(word1.getFirstChild(), word3);
    assertEquals(word1.getLastChild(), word3);

    //Now check word3, shouldn't have any previous/next siblings or first/last child, word1 should be the start/end parent
    assertTrue(word3.hasStartParent());
    assertTrue(word3.hasEndParent());
    assertFalse(word3.hasFirstChild());
    assertFalse(word3.hasLastChild());
    assertFalse(word3.hasPreviousSibling());
    assertFalse(word3.hasNextSibling());

    //word 2 shouldn't have any associations
    assertFalse(word2.hasStartParent());
    assertFalse(word2.hasEndParent());
    assertFalse(word2.hasFirstChild());
    assertFalse(word2.hasFirstChild());
    assertFalse(word2.hasPreviousSibling());
    assertFalse(word2.hasNextSibling());

    //Now lets try and remove word1, should complain because it has a child
    try {
      document.removeBlock(word1);
      fail("word1 was removed even thou it contains word2");
    } catch (BlockStructureCascadeException e) {}

    //Now let's force the issue
    try {
      document.removeBlock(word1, true);
    } catch (BlockStructureCascadeException e) {}

    assertEquals(document.getNumberOfBlocks(), 0);

    //Let's check the words have been left in a good state.
    //no of the words should have any relations to each other
    this.checkBlockHasNoRelations(word1);
    this.checkBlockHasNoRelations(word2);
    this.checkBlockHasNoRelations(word3);

    //Let's try some more things
    try {
      document.setRootBlock(word1);
    } catch (BlockStructureCascadeException e) {
      fail("Could not set root block to word1." + e);
    }
    //This should fail as we already have a root Block
    try {
      document.setRootBlock(word2);
      fail("setRootBlock to word2, but document already has a root Block.");
    } catch (BlockStructureCascadeException e) {}

    assertEquals(1, document.getNumberOfBlocks());

    document.setChildBlock(word1, word2);
    assertEquals(2, document.getNumberOfBlocks());
    assertTrue(word1.hasFirstChild());
    assertTrue(word1.hasLastChild());
    assertEquals(word1.getFirstChild(), word2);
    assertEquals(word1.getLastChild(), word2);
    assertTrue(word2.hasStartParent());
    assertTrue(word2.hasEndParent());
    assertEquals(word2.getStartParent(), word1);
    assertEquals(word2.getEndParent(), word1);

    //Set word3 as the child of word2
    document.setChildBlock(word2, word3);
    assertEquals(3, document.getNumberOfBlocks());
    assertTrue(word2.hasFirstChild());
    assertTrue(word2.hasLastChild());
    assertEquals(word3, word2.getFirstChild());
    assertEquals(word3, word2.getLastChild());
    assertTrue(word3.hasStartParent());
    assertTrue(word3.hasEndParent());
    assertEquals(word2, word3.getStartParent());
    assertEquals(word2, word3.getEndParent());

    //Set word4 to the next sibling of word3
    document.setNextSibling(word3, word4);
    //Check word3 has a next sibling
    assertTrue(word3.hasNextSibling());
    assertEquals(word4, word3.getNextSibling());
    assertFalse(word3.hasPreviousSibling());
    assertTrue(word4.hasPreviousSibling());
    assertEquals(word3, word4.getPreviousSibling());
    assertFalse(word3.hasPreviousSibling());
    //word2 should be the parent of both blocks
    assertTrue(word3.hasStartParent());
    assertEquals(word2, word3.getStartParent());
    assertTrue(word3.hasEndParent());
    assertEquals(word2, word3.getEndParent());
    assertTrue(word4.hasStartParent());
    assertTrue(word4.hasEndParent());
    assertEquals(word2, word4.getStartParent());
    assertEquals(word2, word4.getEndParent());
    //word2 first child should be word3, and its end child should be word4
    assertTrue(word2.hasFirstChild());
    assertEquals(word3, word2.getFirstChild());
    assertTrue(word2.hasLastChild());
    assertEquals(word4, word2.getLastChild());

    //Now lets insert word5 as the previous sibling of word3
    document.setPreviousSibling(word3, word5);
    //Check word3 has a previous sibling
    assertTrue(word3.hasPreviousSibling());
    assertEquals(word5, word3.getPreviousSibling());
    assertTrue(word5.hasNextSibling());
    assertEquals(word3, word5.getNextSibling());
    assertFalse(word5.hasPreviousSibling());
    assertTrue(word3.hasNextSibling());
    assertEquals(word4, word3.getNextSibling());
    //Check the parent
    assertTrue(word5.hasStartParent());
    assertEquals(word2, word5.getStartParent());
    assertTrue(word5.hasEndParent());
    assertEquals(word2, word5.getEndParent());
    assertTrue(word4.hasStartParent());
    assertEquals(word2, word4.getStartParent());
    assertTrue(word4.hasEndParent());
    assertEquals(word2, word4.getEndParent());
    assertTrue(word2.hasFirstChild());
    assertEquals(word5, word2.getFirstChild());
    assertTrue(word2.hasLastChild());
    assertEquals(word4, word2.getLastChild());
  }

  public void checkBlockHasNoRelations(Block b) {
    assertFalse(b.hasFirstChild());
    assertFalse(b.hasLastChild());
    assertFalse(b.hasStartParent());
    assertFalse(b.hasEndParent());
    assertFalse(b.hasPreviousSibling());
    assertFalse(b.hasNextSibling());
  }

}
