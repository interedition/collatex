package com.sd_editions.collatex.spike2;

import junit.framework.TestCase;

public class WitnessTokenizerTest extends TestCase {
  public void test1() {
    String witness = "A black cat.";
    WitnessTokenizer tokenizer = new WitnessTokenizer(witness);
    assertTrue(tokenizer.hasNextToken());
    assertEquals("A", tokenizer.nextToken());
    assertTrue(tokenizer.hasNextToken());
    assertEquals("black", tokenizer.nextToken());
    assertTrue(tokenizer.hasNextToken());
    assertEquals("cat.", tokenizer.nextToken());
    assertFalse(tokenizer.hasNextToken());
  }
}
