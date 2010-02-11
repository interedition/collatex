package eu.interedition.collatex.experimental.ngrams;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import eu.interedition.collatex.experimental.ngrams.data.Witness;

public class NGramIndexTest {

  @Test
  public void testNGramIndex() {
    final Witness a = new Witness("A", "a b c d");
    final BiGramIndex index = BiGramIndex.create(a);
    final List<NGram> ngrams = NGramIndex.concatenateBiGramToNGram(index);
    Assert.assertEquals(1, ngrams.size());
  }

  //  @Test
  //  public void testNGramIndex() {
  //    final Witness a = new Witness("A", "a b c d GAP e f g");
  //    final BiGramIndex biGramI = BiGramIndex.create(a);
  //    biGramI.removeBiGramsWithToken(new Token("A", "GAP", 5));
  //  }
}
