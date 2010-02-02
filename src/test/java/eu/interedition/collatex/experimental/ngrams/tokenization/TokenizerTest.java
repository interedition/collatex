package eu.interedition.collatex.experimental.ngrams.tokenization;

import junit.framework.Assert;

import org.junit.Test;

import eu.interedition.collatex.experimental.ngrams.data.Witness;

public class TokenizerTest {
  //TODO: do some with preceding whitespace!
  // test content
  @Test
  public void testTokenizer1() {
    final Witness witness = new Witness("1859", "WHEN we look to the individuals of the");
    final Tokenizer tokenizer = new Tokenizer(witness);
    Assert.assertEquals("WHEN", tokenizer.nextToken().getContent());
    Assert.assertEquals("we", tokenizer.nextToken().getContent());
    Assert.assertEquals("look", tokenizer.nextToken().getContent());
    Assert.assertTrue(tokenizer.hasNext());
  }
}
