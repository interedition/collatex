package eu.interedition.collatex.experimental.ngrams;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import eu.interedition.collatex.experimental.interfaces.WitnessF;
import eu.interedition.collatex.experimental.ngrams.data.Token;
import eu.interedition.collatex2.interfaces.IWitness;

public class NGramIndexTest {

  @Test
  public void testNGramIndex() {
    final IWitness a = WitnessF.create("A", "a b c d");
    final BiGramIndex index = BiGramIndex.create(a);
    final List<NGram> ngrams = NGramIndex.concatenateBiGramToNGram(index);
    Assert.assertEquals(1, ngrams.size());
    // Note: next assert checks that the original index
    // Note: is not affected!
    Assert.assertEquals(5, index.size());
  }

  @Test
  public void testNGramIndex2() {
    final IWitness a = WitnessF.create("A", "a b c d GAP e f g");
    final BiGramIndex biGramI = BiGramIndex.create(a);
    final BiGramIndex indexWithGap = biGramI.removeBiGramsWithToken(new Token("A", "GAP", 5));
    final List<NGram> ngrams = NGramIndex.concatenateBiGramToNGram(indexWithGap);
    Assert.assertEquals(2, ngrams.size());

  }
}
