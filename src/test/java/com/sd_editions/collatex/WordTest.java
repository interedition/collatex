package com.sd_editions.collatex;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for Word Word.
 */
public class WordTest extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public WordTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( WordTest.class );
    }

    /**
     * Simple check that we can set and get text content
     */
    public void testCreateWord()
    {
	  String textContent = "some content";
	  Word tword = new Word(textContent);
	  assertNotNull( tword );

	  assertEquals(tword.getContent(), textContent);
    }

	/**
	 * Simple check of setting/getting attributes and things
	 */
	public void testWordAttributes()
	{
	  String textContent = "text content";
	  Word tword = new Word(textContent);

	  String attrName1 = "aname1";
	  String attrValue1 = "avalue1";
	  String attrName2 = "aname2";
	  String attrValue2 = "avalue2";

	  //Set the attributes
	  tword.put(attrName1, attrValue1);
	  tword.put(attrName2, attrValue2);

	  //We do have two attributes don't we?
	  assertEquals(tword.size(), 2);

	  //Check that they are right
	  assertEquals(tword.get(attrName1), attrValue1);
	  assertEquals(tword.get(attrName2), attrValue2);

	  //Try removing one
	  tword.remove(attrName1);
	  assertEquals(tword.size(), 1);

	  //Try removing the last
	  tword.remove(attrName2);
	  assertEquals(tword.size(), 0);
	}

	public void testWordRelationships() {
	  String firstContent = "First Word";
	  String twoContent = "Second Word";
	  String threeContent = "Third Word";

	  Word word1 = new Word(firstContent);
	  Word word2 = new Word(twoContent);
	  Word word3 = new Word(threeContent);

	  word1.setFirstChild(word2);
	  assertEquals(word1.getFirstChild(), word2);
	  word1.removeFirstChild();
	  assertNull(word1.getFirstChild());

	  word1.setLastChild(word3);
	  assertEquals(word1.getLastChild(), word3);
	  word1.removeLastChild();
	  assertNull(word1.getLastChild());

	  word1.setNextSibling(word2);
	  assertEquals(word1.getNextSibling(), word2);
	  word1.removeNextSibling();
	  assertNull(word1.getNextSibling());

	  word1.setPreviousSibling(word3);
	  assertEquals(word1.getPreviousSibling(), word3);
	  word1.removePreviousSibling();
	  assertNull(word1.getPreviousSibling());

	  word1.setStartParent(word2);
	  assertEquals(word1.getStartParent(), word2);
	  word1.removeStartParent();
	  assertNull(word1.getStartParent());

	  word1.setEndParent(word3);
	  assertEquals(word1.getEndParent(), word3);
	  word1.removeEndParent();
	  assertNull(word1.getEndParent());
	}
<<<<<<< HEAD:src/test/java/com/sd_editions/collatex/WordTest.java

	public void testFirstChild() {
	  String wordContent = new String("test content");
	  Word word1 = new Word(wordContent);
	  Word word2 = new Word(wordContent);
	  Word word3 = new Word(wordContent);

	  //Set the first child
	  word1.setFirstChild(word2);
	  assertTrue(word1.hasFirstChild());
	  assertEquals(word1.getFirstChild(), word2);
	  assertEquals(word2.getStartParent(), word1);

	  //Remove the child
	  word1.removeFirstChild();
	  assertFalse(word1.hasFirstChild());
	  assertNull(word1.getFirstChild());
	  assertFalse(word2.hasStartParent());

	  word1.setFirstChild(word2);
	  //Now reset the first child, the previous first child should be pushed to this childs next sibling
	  word1.setFirstChild(word3);
	  //Hopefully we should still have a first child
	  assertTrue(word1.hasFirstChild());
	  //The first child should be word3
	  assertEquals(word1.getFirstChild(), word3);
	  //The first child should have a next sibling, word2
	  assertTrue(word1.getFirstChild().hasNextSibling());
	  //The first child shouldn't have a previous sibling
	  assertFalse(word1.getFirstChild().hasPreviousSibling());
	  //The first childs next sibling should be word2
	  assertEquals(word1.getFirstChild().getNextSibling(), word2);
	  //The first childs startParent should be word1
	  assertTrue(word1.getFirstChild().hasStartParent());
	  assertEquals(word1, word1.getFirstChild().getStartParent());
	  //The first childs endParent should be word1
	  assertTrue(word1.getFirstChild().hasEndParent());
	  assertEquals(word1, word1.getFirstChild().getEndParent());
	  //The first childs next sibling's start parent should be word1
	  assertTrue(word1.getFirstChild().getNextSibling().hasStartParent());
	  assertEquals(word1, word1.getFirstChild().getNextSibling().getStartParent());
	  //The first childs next sibling's endParent should be word1
	  assertTrue(word1.getFirstChild().getNextSibling().hasEndParent());
	  assertEquals(word1, word1.getFirstChild().getNextSibling().getEndParent());
	  //The first child's next sibling's previous sibling should be word3
	  assertEquals(word1.getFirstChild().getNextSibling().getPreviousSibling(), word3);
	  //Let's check the individual pieces
	  //word1 should have a child but no next/previous siblings
	  assertTrue(word1.hasFirstChild());
	  assertFalse(word1.hasPreviousSibling());
	  assertFalse(word1.hasNextSibling());
	  assertFalse(word1.hasStartParent());
	  assertFalse(word1.hasEndParent());
	  //word2 should not have a child or a next sibling but should have a previous sibling
	  assertFalse(word2.hasFirstChild());
	  assertFalse(word2.hasNextSibling());
	  assertTrue(word2.hasPreviousSibling());
	  assertTrue(word2.hasStartParent());
	  assertEquals(word1, word2.getStartParent());
	  assertTrue(word2.hasEndParent());
	  assertEquals(word1, word2.getEndParent());
	  //word3 should not have a child or previous sibling, but should have a next sibling
	  assertFalse(word3.hasFirstChild());
	  assertTrue(word3.hasNextSibling());
	  assertFalse(word3.hasPreviousSibling());
	  assertTrue(word3.hasStartParent());
	  assertEquals(word1, word3.getStartParent());
	  assertTrue(word3.hasEndParent());
	  assertEquals(word1, word3.getEndParent());

	  //Now remove the child (word3)
	  word1.removeFirstChild();
	  assertTrue(word1.hasFirstChild());
	  assertEquals(word1.getFirstChild(), word2);
	  assertFalse(word1.getFirstChild().hasNextSibling());
	  assertFalse(word1.getFirstChild().hasPreviousSibling());

	  //word3 should not have a child or previous sibling or next sibling
	  assertFalse(word3.hasFirstChild());
	  assertFalse(word3.hasNextSibling());
	  assertFalse(word3.hasPreviousSibling());
	  assertFalse(word3.hasStartParent());
	  assertFalse(word3.hasEndParent());

	  //word2 should not have a child, previous or next sibling
	  assertFalse(word2.hasFirstChild());
	  assertFalse(word2.hasNextSibling());
	  assertFalse(word2.hasPreviousSibling());
	  assertTrue(word2.hasStartParent());
	  assertEquals(word1, word2.getStartParent());
	  assertTrue(word2.hasEndParent());
	  assertEquals(word1, word2.getEndParent());

	  //word1 should have a child, but no next/previous sibling
	  assertTrue(word1.hasFirstChild());
	  assertEquals(word1.getFirstChild(), word2);
	  assertFalse(word1.hasNextSibling());
	  assertFalse(word1.hasPreviousSibling());
	  assertFalse(word1.hasStartParent());
	  assertFalse(word1.hasEndParent());

	  //Remove the remaining child
	  word1.removeFirstChild();
	  assertFalse(word1.hasFirstChild());

	  assertFalse(word2.hasStartParent());
	  assertFalse(word2.hasEndParent());
	  assertFalse(word2.hasPreviousSibling());
	  assertFalse(word2.hasNextSibling());
	  assertFalse(word2.hasFirstChild());

	  assertFalse(word3.hasStartParent());
	  assertFalse(word3.hasEndParent());
	  assertFalse(word3.hasPreviousSibling());
	  assertFalse(word3.hasNextSibling());
	  assertFalse(word3.hasFirstChild());
	}

	/**
	 * test previousSiblings
	 *
	 */
	public void testPreviousSibling() {
	  String wordContent = new String("test content");
	  Word word1 = new Word(wordContent);
	  Word word2 = new Word(wordContent);
	  Word word3 = new Word(wordContent);

	  assertFalse(word1.hasPreviousSibling());
	  assertFalse(word0.hasNextSibling());

	  assertFalse(word2.hasPreviousSibling());
	  assertFalse(word2.hasNextSibling());

	  assertFalse(word2.hasPreviousSibling());
	  assertFalse(word2.hasNextSibling());
	  
	  //Set word2 to the previous sibling of word1
	  word1.setPreviousSibling(word2);
	  assertTrue(word1.hasPreviousSibling());
	  assertEquals(word1.getPreviousSibling(), word2);
	  assertTrue(word1.hasNextSibling());
	  assertEquals(word2.getNextSibling(), word1);

	}
=======
>>>>>>> 458ee4e... More changes to Block.java and it's test suite.:src/test/java/com/sd_editions/collatex/WordTest.java
}
