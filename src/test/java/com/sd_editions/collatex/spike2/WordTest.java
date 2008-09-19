package com.sd_editions.collatex.spike2;

import junit.framework.TestCase;

public class WordTest extends TestCase {
  public void testNormalize() {
    Word word = new Word("Hello,", 1);
    assertEquals("hello", word.normalized);
  }
}
