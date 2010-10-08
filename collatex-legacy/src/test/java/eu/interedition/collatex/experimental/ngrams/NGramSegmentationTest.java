/**
 * CollateX - a Java library for collating textual sources,
 * for example, to produce an apparatus.
 *
 * Copyright (C) 2010 ESF COST Action "Interedition".
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.interedition.collatex.experimental.ngrams;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import eu.interedition.collatex.input.Witness;
import eu.interedition.collatex.input.builders.WitnessBuilder;

public class NGramSegmentationTest {
  private WitnessBuilder builder;

  @Before
  public void before() {
    builder = new WitnessBuilder();
  }

  @Ignore
  @Test
  public void testBeckett1() {
    final Witness a = builder.build("A", "as when clock");
    //    final List<Phrase> bigrams = BiGrams.calculate(a);
    //    Assert.assertEquals(2, bigrams.size());
    //    final Phrase bigram1 = bigrams.get(0);
    //    final Phrase bigram2 = bigrams.get(1);
    // TODO should be bigram1.getNormalized()
    //    Assert.assertEquals("as when", bigram1.getOriginal());
    //    Assert.assertEquals("when clock", bigram2.getOriginal());
    // THIS should become 4 bigrams; # as, as when, when clock, clock #
  }

  // TODO clock should get its own subsegment! (not overlapping)
  //  @Ignore
  //  @Test
  //  public void testBeckett2() {
  //    final Witness a = builder.build("A", "as when clock");
  //    final Witness b = builder.build("B", "as when");
  //    final List<Subsegment2> subsegments = BiGrams.getOverlappingBiGrams(a, b);
  //    Assert.assertEquals(1, subsegments.size());
  //    final Subsegment2 segment1 = subsegments.get(0);
  //    Assert.assertEquals("as when", segment1.getNormalized());
  //    // Overlapping subsegments should become # as, as when
  //  }

  // I would want a test that tests additions and replacements
  // additions are almost the same as replacements in the new way of thinking
  // hummm omissions are a form of additions ... so ...
  // so thinking about the replacements first..

  // TODO add extra asserts
  // NOTE: # as, as when
  // NOTE: when clock, clock #
  //   public void testBeckett2a() {
  //    final Witness a = builder.build("A", "# as when clock #");
  //    final Witness b = builder.build("B", "# as when #");
  //    final List<Subsegment2> subsegments = BiGrams.getOverlappingBiGrams(a, b);
  //    Assert.assertEquals(2, subsegments.size());
  //    //    final List<Phrase> uniqueSubsegmentsOne = BiGrams.getUniqueBiGramsForWitnessA(a, b);
  //    //    Assert.assertEquals(2, uniqueSubsegmentsOne.size());
  //    //    final Phrase segment1 = uniqueSubsegmentsOne.get(0);
  //    //    final Phrase segment2 = uniqueSubsegmentsOne.get(1);
  //    //    Assert.assertEquals("when clock", segment1.getOriginal());
  //    //    Assert.assertEquals("clock #", segment2.getOriginal());
  //    //    final List<Phrase> uniqueSubsegmentsTwo = BiGrams.getUniqueBiGramsForWitnessB(a, b);
  //    //    Assert.assertEquals(1, uniqueSubsegmentsTwo.size());
  //    //    final Phrase segment3 = uniqueSubsegmentsTwo.get(0);
  //    //    Assert.assertEquals("when #", segment3.getOriginal());
  //  }
  // TODO next step would be to make trigrams from bigrams were possible for each witness!
  //  @Test
  //  @Ignore
  //  public void testBeckett2b() {
  //    final Witness a = builder.build("A", "# as when clock #");
  //    final Witness b = builder.build("B", "# as when #");
  //    final List<Phrase> pieces = BiGrams.getLongestUniquePiecesForWitnessA(a, b);
  //    Assert.assertEquals(1, pieces.size());
  //    final Phrase piece = pieces.get(0);
  //    Assert.assertEquals("when clock #", piece.getOriginal());
  //    final List<Phrase> pieces2 = BiGrams.getLongestUniquePiecesForWitnessB(a, b);
  //    Assert.assertEquals(1, pieces2.size());
  //    final Phrase piece2 = pieces2.get(0);
  //    Assert.assertEquals("when #", piece2.getOriginal());
  //  }
  // TODO nu is het een kwestie van de groepen met elkaar vergelijken
  // ok nu moet er een nieuwe vorm van gap detection komen
  // die dus gaps oplevert
  // interesting
  // Not sure where this test leads to
  @Ignore
  @Test
  public void testBeckett3() {
    final Witness a = builder.build("A", "as when clock");
    final Witness b = builder.build("B", "as when");
    //    final WitnessSegmentPhrases wsp1 = BiGrams.getWSP("A", a, b);
    //    final WitnessSegmentPhrases wsp2 = BiGrams.getWSP("B", a, b);
    //    Assert.assertEquals("|as when|", wsp2.toSpecialString());
    // TODO add test for wsp1!
  }
}
