package com.sd_editions.collatex.spike2;

import junit.framework.TestCase;

public class IndexTest extends TestCase {
  public void testVerySimple() {
    String[] witnesses = new String[] { "very simple", "simple indeed" };
    Index index = new Index(witnesses);
    assertEquals(3, index.numberOfEntries());
  }

  public void testNormalize() {
    String[] witnesses = new String[] { "Normalize  normalize" };
    Index index = new Index(witnesses);
    assertEquals(1, index.numberOfEntries());
  }
}
