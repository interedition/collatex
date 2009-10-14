package eu.interedition.collatex.input;

import junit.framework.TestCase;

public class WordTest extends TestCase {
  private final static String witnessId = "A";

  public void testNormalize1() {
    Word word = new Word(witnessId, "Hello,", 1);
    assertEquals("hello", word.normalized);
  }

  public void testNormalize2() {
    Word word = new Word(witnessId, "ειπων", 2);
    assertEquals("ειπων", word.normalized);
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
