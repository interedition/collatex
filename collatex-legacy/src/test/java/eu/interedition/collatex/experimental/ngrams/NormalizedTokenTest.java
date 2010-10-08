package eu.interedition.collatex.experimental.ngrams;

import org.junit.Assert;
import org.junit.Test;

import eu.interedition.collatex.experimental.ngrams.data.NormalizedToken;
import eu.interedition.collatex.experimental.ngrams.data.Token;
import eu.interedition.collatex2.interfaces.INormalizedToken;

public class NormalizedTokenTest {
  @Test
  public void testABC() {
    final Token token = new Token("A", "NotNormalized!...", 1);
    final INormalizedToken result = NormalizedToken.normalize(token);
    Assert.assertEquals("notnormalized", result.getNormalized());
  }

  @Test
  public void testPunctuationToken() {
    final Token token = new Token("P", "#@$!", 1);
    final INormalizedToken result = NormalizedToken.normalize(token);
    Assert.assertEquals("#@$!", result.getNormalized());
  }
}
