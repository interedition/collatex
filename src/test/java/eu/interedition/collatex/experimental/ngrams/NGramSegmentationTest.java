package eu.interedition.collatex.experimental.ngrams;

import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import eu.interedition.collatex.input.Phrase;
import eu.interedition.collatex.input.Witness;
import eu.interedition.collatex.input.WitnessSegmentPhrases;
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
    // THIS should become 4 bigrams; # as, as when, when clock, clock #
  }

  // TODO: clock should get its own subsegment! (not overlapping)
  @Test
  public void testBeckett2() {
    final Witness a = builder.build("A", "as when clock");
    final Witness b = builder.build("B", "as when");
    final List<Subsegment2> subsegments = BiGrams.getOverlappingBiGrams(a, b);
    Assert.assertEquals(1, subsegments.size());
    final Subsegment2 segment1 = subsegments.get(0);
    Assert.assertEquals("as when", segment1.getNormalized());
    // Overlapping subsegments should become # as, as when
  }

  // I would want a test that tests additions and replacements
  // additions are almost the same as replacements in the new way of thinking
  // hummm omissions are a form of additions ... so ...
  // so thinking about the replacements first..

  // TODO: add extra asserts
  // NOTE: # as, as when
  // NOTE: when clock, clock #
  @Test
  public void testBeckett2a() {
    final Witness a = builder.build("A", "# as when clock #");
    final Witness b = builder.build("B", "# as when #");
    final List<Subsegment2> subsegments = BiGrams.getOverlappingBiGrams(a, b);
    Assert.assertEquals(2, subsegments.size());
    final List<Phrase> uniqueSubsegmentsOne = BiGrams.getUniqueBiGramsForWitnessA(a, b);
    Assert.assertEquals(2, uniqueSubsegmentsOne.size());
    final Phrase segment1 = uniqueSubsegmentsOne.get(0);
    final Phrase segment2 = uniqueSubsegmentsOne.get(1);
    Assert.assertEquals("when clock", segment1.getOriginal());
    Assert.assertEquals("clock #", segment2.getOriginal());
    final List<Phrase> uniqueSubsegmentsTwo = BiGrams.getUniqueBiGramsForWitnessB(a, b);
    Assert.assertEquals(1, uniqueSubsegmentsTwo.size());
    final Phrase segment3 = uniqueSubsegmentsTwo.get(0);
    Assert.assertEquals("when #", segment3.getOriginal());
  }

  // TODO: next step would be to make trigrams from bigrams were possible for each witness!

  // Not sure where this test leads to
  @Ignore
  @Test
  public void testBeckett3() {
    final Witness a = builder.build("A", "as when clock");
    final Witness b = builder.build("B", "as when");
    final WitnessSegmentPhrases wsp1 = BiGrams.getWSP("A", a, b);
    final WitnessSegmentPhrases wsp2 = BiGrams.getWSP("B", a, b);
    Assert.assertEquals("|as when|", wsp2.toSpecialString());
    // TODO: add test for wsp1!
  }
}
