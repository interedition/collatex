package eu.interedition.collatex.experimental.ngrams;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import eu.interedition.collatex.experimental.ngrams.data.NormalizedToken;
import eu.interedition.collatex.experimental.ngrams.data.NormalizedWitness;
import eu.interedition.collatex.experimental.ngrams.data.Token;
import eu.interedition.collatex.experimental.ngrams.data.Witness;
import eu.interedition.collatex.experimental.ngrams.tokenization.NormalizedWitnessBuilder;

public class NGramTest {

  @Test
  public void testNGram() {
    final Witness a = new Witness("A", "The black and white cat");
    final NormalizedWitness aa = NormalizedWitnessBuilder.create(a);
    final List<NormalizedToken> tokens = aa.getTokens(1, 2);
    final Token token = new Token("A", "The", 1);
    final Token token2 = new Token("A", "black", 2);

    Assert.assertEquals(2, tokens.size());
    Assert.assertEquals(token, tokens.get(0));
    Assert.assertEquals(token2, tokens.get(1));
  }
}
