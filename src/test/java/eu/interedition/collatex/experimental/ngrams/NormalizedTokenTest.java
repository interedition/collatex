package eu.interedition.collatex.experimental.ngrams;

import org.junit.Assert;
import org.junit.Test;

import eu.interedition.collatex.experimental.ngrams.data.NormalizedToken;
import eu.interedition.collatex.experimental.ngrams.data.Token;

public class NormalizedTokenTest {
  @Test
  public void testABC() {
    final Token token = new Token("A", "NotNormalized!...", 1);
    final NormalizedToken result = NormalizedToken.normalize(token);
    Assert.assertEquals("notnormalized", result.getNormalized());
  }
}
