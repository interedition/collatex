package eu.interedition.collatex.experimental.ngrams.tokenization;

import junit.framework.Assert;

import org.junit.Test;

public class TokenizerTest {
  //TODO do some with preceding whitespace!
  // test content
  @Test
  public void testTokenizer1() {
    final Tokenizer tokenizer = new Tokenizer("1859", "WHEN we look to the individuals of the");
    Assert.assertEquals("WHEN", tokenizer.nextToken().getContent());
    Assert.assertEquals("we", tokenizer.nextToken().getContent());
    Assert.assertEquals("look", tokenizer.nextToken().getContent());
    Assert.assertTrue(tokenizer.hasNext());
  }
}
