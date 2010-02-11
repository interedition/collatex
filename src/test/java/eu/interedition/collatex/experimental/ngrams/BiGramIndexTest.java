package eu.interedition.collatex.experimental.ngrams;

import junit.framework.Assert;

import org.junit.Test;

import eu.interedition.collatex.experimental.ngrams.data.Token;
import eu.interedition.collatex.experimental.ngrams.data.Witness;

public class BiGramIndexTest {

  @Test
  public void testCreate() {
    final Witness a = new Witness("A", "a b c d");
    final BiGramIndex index = BiGramIndex.create(a);
    Assert.assertEquals(5, index.size());
  }

  @Test
  public void testRemoveTokenFromIndex() {
    final Witness a = new Witness("A", "a b c d");
    final BiGramIndex index = BiGramIndex.create(a);
    final BiGramIndex result = index.removeBiGramsWithToken(new Token("A", "c", 3));
    Assert.assertEquals(3, result.size());
  }
}
