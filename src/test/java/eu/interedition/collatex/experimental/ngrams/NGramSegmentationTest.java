package eu.interedition.collatex.experimental.ngrams;

import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import eu.interedition.collatex.input.Phrase;
import eu.interedition.collatex.input.Witness;
import eu.interedition.collatex.input.builders.WitnessBuilder;

public class NGramSegmentationTest {
  private WitnessBuilder builder;

  @Before
  public void before() {
    builder = new WitnessBuilder();
  }

  @Test
  public void testBeckett1() {
    final Witness a = builder.build("A", "as when clock");
    final List<Phrase> bigrams = BiGrams.calculate(a);
    Assert.assertEquals(2, bigrams.size());
    final Phrase bigram1 = bigrams.get(0);
    final Phrase bigram2 = bigrams.get(1);
    // TODO: should be bigram1.getNormalized()
    Assert.assertEquals("as when", bigram1.getOriginal());
    Assert.assertEquals("when clock", bigram2.getOriginal());
  }

  @Test
  public void testBeckett2() {
    final Witness a = builder.build("A", "as when clock");
    final Witness b = builder.build("B", "as when");
    final List<Subsegment2> subsegments = BiGrams.getOverlappingBiGrams(a, b);
    Assert.assertEquals(1, subsegments.size());
    final Subsegment2 segment1 = subsegments.get(0);
    Assert.assertEquals("as when", segment1.getNormalized());
  }
}
