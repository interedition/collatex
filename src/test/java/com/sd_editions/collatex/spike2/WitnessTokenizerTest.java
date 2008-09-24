package com.sd_editions.collatex.spike2;

import junit.framework.TestCase;

public class WitnessTokenizerTest extends TestCase {
  public void test1() {
    String witness = "A black cat.";
    WitnessTokenizer tokenizer = new WitnessTokenizer(witness, true);
    assertTrue(tokenizer.hasNextToken());
    assertEquals("a", tokenizer.nextToken());
    assertTrue(tokenizer.hasNextToken());
    assertEquals("black", tokenizer.nextToken());
    assertTrue(tokenizer.hasNextToken());
    assertEquals("cat", tokenizer.nextToken());
    assertFalse(tokenizer.hasNextToken());
  }

  public void test2() {
    String witness = "Alas, Horatio, I knew him well.";
    WitnessTokenizer tokenizer = new WitnessTokenizer(witness, false);
    assertTrue(tokenizer.hasNextToken());
    assertEquals("Alas,", tokenizer.nextToken());
    assertTrue(tokenizer.hasNextToken());
    assertEquals("Horatio,", tokenizer.nextToken());
    assertTrue(tokenizer.hasNextToken());
    assertEquals("I", tokenizer.nextToken());
  }

  public void test3() {
    String witness = "ταυτα ειπων ο ιϲ̅ εξηλθεν ϲυν τοιϲ μαθηταιϲ αυτου περαν του χειμαρρου των κεδρων οπου ην κηποϲ ειϲ ον ειϲηλθεν αυτοϲ και οι μαθηται αυτου";
    WitnessTokenizer tokenizer = new WitnessTokenizer(witness, false);
    assertTrue(tokenizer.hasNextToken());
    assertEquals("ταυτα", tokenizer.nextToken());
    assertTrue(tokenizer.hasNextToken());
    assertEquals("ειπων", tokenizer.nextToken());
    assertTrue(tokenizer.hasNextToken());
    assertEquals("ο", tokenizer.nextToken());
  }
}
