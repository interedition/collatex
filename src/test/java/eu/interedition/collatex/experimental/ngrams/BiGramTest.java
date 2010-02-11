package eu.interedition.collatex.experimental.ngrams;

import org.junit.Assert;
import org.junit.Test;

import eu.interedition.collatex.experimental.ngrams.data.Token;

public class BiGramTest {
  @Test
  public void testContains() {
    final Token token = new Token("A", "token", 1);
    final Token token2 = new Token("A", "token2", 2);
    final Token token3 = new Token("B", "token", 1);
    final BiGram bigram = BiGram.create(token, token2);
    Assert.assertTrue(bigram.contains(token));
    Assert.assertFalse(bigram.contains(token3));
  }
}
