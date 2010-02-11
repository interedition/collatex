package eu.interedition.collatex.experimental.ngrams;

import org.junit.Assert;
import org.junit.Test;

import eu.interedition.collatex.experimental.ngrams.data.Token;

public class TokenTest {

  @Test
  public void testEquals1() {
    final Token token = new Token("A", "token", 1);
    final Token token2 = new Token("A", "token", 1);
    final Token token3 = new Token("B", "token", 1);
    Assert.assertEquals(token, token2);
    Assert.assertFalse(token.equals(token3));
  }
}
