package eu.interedition.collatex.experimental.ngrams;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import eu.interedition.collatex.experimental.ngrams.data.NormalizedToken;
import eu.interedition.collatex.experimental.ngrams.data.Token;
import eu.interedition.collatex.experimental.ngrams.tokenization.NormalizedWitnessBuilder;
import eu.interedition.collatex.interfaces.IWitness;
import eu.interedition.collatex.interfaces.WitnessF;

public class NGramTest {

  // TODO: why is this called NGramTest: NGram isn't even used!
  // probably because of the NormalizedWitness.getTokens(1,2) which is used
  // for the creation of NGrams
  @Test
  public void testNGram() {
    final IWitness aa = NormalizedWitnessBuilder.create("A", "The black and white cat");
    final List<NormalizedToken> tokens = aa.getTokens(1, 2);
    final Token token = new Token("A", "The", 1);
    final Token token2 = new Token("A", "black", 2);

    Assert.assertEquals(2, tokens.size());
    Assert.assertEquals(token, tokens.get(0));
    Assert.assertEquals(token2, tokens.get(1));
  }

  @Test
  public void testNGram2Trim() {
    final IWitness a = WitnessF.create("A", "The black and white cat");
    final List<NormalizedToken> tokens = a.getTokens();
    final NGram ngram = new NGram(tokens);
    final NGram result = ngram.trim();
    Assert.assertEquals("black and white", result.getNormalized());
    // TODO: maybe assert size()?
  }

}
