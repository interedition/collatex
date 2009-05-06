package com.sd_editions.collatex.permutations;

import junit.framework.TestCase;

public class WordTest extends TestCase {
  private final String witnessId = "A";

  public void testNormalize1() {
    Word word = new Word(witnessId, "Hello,", 1);
    assertEquals("hello", word.normalized);
  }

  public void testNormalize2() {
    Word word = new Word(witnessId, "ειπων", 2);
    assertEquals("ειπων", word.normalized);
  }
}
