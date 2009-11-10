package eu.interedition.collatex.alignment;

import java.util.Set;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import com.sd_editions.collatex.match.SubsegmentExtractor;

import eu.interedition.collatex.input.Phrase;
import eu.interedition.collatex.input.Segment;
import eu.interedition.collatex.input.WitnessSegmentPhrases;
import eu.interedition.collatex.input.builders.WitnessBuilder;
import eu.interedition.collatex.matching.LeftToRightMatcher;

public class PhraseGapDetectionTest {
  private WitnessBuilder builder;

  @Before
  public void setup() {
    builder = new WitnessBuilder();
  }

  @Test
  public void testNoGaps() {
    final Segment a = builder.build("a", "everything matches").getFirstSegment();
    final Segment b = builder.build("b", "everything matches").getFirstSegment();
    final SubsegmentExtractor sse = new SubsegmentExtractor(a, b);
    sse.go();
    final WitnessSegmentPhrases pa = sse.getWitnessSegmentPhrases("a");
    final WitnessSegmentPhrases pb = sse.getWitnessSegmentPhrases("b");

    Assert.assertEquals(1, pa.size());
    Assert.assertEquals(1, pb.size());

    final Set<Match<Phrase>> matches = LeftToRightMatcher.match(pa, pb);
    final Alignment<Phrase> alignment = Alignment.createPhraseAlignment(matches, pa, pb);
    Assert.assertEquals(0, alignment.getGaps().size());
  }

  @Test
  public void testAdditionInFront() {
    final Segment a = builder.build("a", "everything matches").getFirstSegment();
    final Segment b = builder.build("b", "addition everything matches").getFirstSegment();
    final SubsegmentExtractor sse = new SubsegmentExtractor(a, b);
    sse.go();
    final WitnessSegmentPhrases pa = sse.getWitnessSegmentPhrases("a");
    final WitnessSegmentPhrases pb = sse.getWitnessSegmentPhrases("b");

    Assert.assertEquals(1, pa.size());
    Assert.assertEquals(2, pb.size());

    final Set<Match<Phrase>> matches = LeftToRightMatcher.match(pa, pb);
    final Alignment<Phrase> alignment = Alignment.createPhraseAlignment(matches, pa, pb);
    final Gap gap = alignment.getAdditions().get(0);
    Assert.assertEquals("NonMatch: addition: true base: ; nextWord: everything matches; witness: addition", gap.toString());
  }

  @Test
  public void testAdditionInTheMiddle() {
    final Segment a = builder.build("a", "everything matches").getFirstSegment();
    final Segment b = builder.build("b", "everything addition matches").getFirstSegment();
    final SubsegmentExtractor sse = new SubsegmentExtractor(a, b);
    sse.go();
    final WitnessSegmentPhrases pa = sse.getWitnessSegmentPhrases("a");
    final WitnessSegmentPhrases pb = sse.getWitnessSegmentPhrases("b");

    Assert.assertEquals(2, pa.size());
    Assert.assertEquals(3, pb.size());

    final Set<Match<Phrase>> matches = LeftToRightMatcher.match(pa, pb);
    final Alignment<Phrase> alignment = Alignment.createPhraseAlignment(matches, pa, pb);
    final Gap gap = alignment.getAdditions().get(0);
    Assert.assertEquals("NonMatch: addition: true base: ; nextWord: matches; witness: addition", gap.toString());
  }

  @Test
  public void testMultipleWordAdditionInTheMiddle() {
    final Segment a = builder.build("a", "everything matches").getFirstSegment();
    final Segment b = builder.build("b", "everything multiple word addition matches").getFirstSegment();
    final SubsegmentExtractor sse = new SubsegmentExtractor(a, b);
    sse.go();
    final WitnessSegmentPhrases pa = sse.getWitnessSegmentPhrases("a");
    final WitnessSegmentPhrases pb = sse.getWitnessSegmentPhrases("b");

    Assert.assertEquals(2, pa.size());
    Assert.assertEquals(5, pb.size());
    // TODO: this is wrong! SHOULD BE 3!

    final Set<Match<Phrase>> matches = LeftToRightMatcher.match(pa, pb);
    final Alignment<Phrase> alignment = Alignment.createPhraseAlignment(matches, pa, pb);
    final Gap gap = alignment.getAdditions().get(0);
    Assert.assertEquals("NonMatch: addition: true base: ; nextWord: matches; witness: multiple word addition", gap.toString());
  }
  //
  //  @Test
  //  public void testAdditionAtTheEnd() {
  //    final Segment a = builder.build("a", "everything matches").getFirstSegment();
  //    final Segment b = builder.build("b", "everything matches addition").getFirstSegment();
  //    final SubsegmentExtractor sse = new SubsegmentExtractor(a, b);
  //    sse.go();
  //    final WitnessSegmentPhrases pa = sse.getWitnessSegmentPhrases("a");
  //    final WitnessSegmentPhrases pb = sse.getWitnessSegmentPhrases("b");
  //
  //    Assert.assertEquals(1, pa.size());
  //    Assert.assertEquals(2, pb.size());
  //
  //    final Set<Match<Phrase>> matches = LeftToRightMatcher.match(pa, pb);
  //    Assert.assertEquals(1, matches.size());
  //    final Match<Phrase> match = matches.iterator().next();
  //    Assert.assertEquals(1, match.getBaseWord().getStartPosition());
  //    Assert.assertEquals(1, match.getWitnessWord().getStartPosition());
  //  }
  //
  //  @Test
  //  public void testOmittedInFront() {
  //    final Segment a = builder.build("a", "omitted everything matches").getFirstSegment();
  //    final Segment b = builder.build("b", "everything matches").getFirstSegment();
  //    final SubsegmentExtractor sse = new SubsegmentExtractor(a, b);
  //    sse.go();
  //    final WitnessSegmentPhrases pa = sse.getWitnessSegmentPhrases("a");
  //    final WitnessSegmentPhrases pb = sse.getWitnessSegmentPhrases("b");
  //
  //    Assert.assertEquals(2, pa.size());
  //    Assert.assertEquals(1, pb.size());
  //
  //    final Set<Match<Phrase>> matches = LeftToRightMatcher.match(pa, pb);
  //    Assert.assertEquals(1, matches.size());
  //    final Match<Phrase> match = matches.iterator().next();
  //    Assert.assertEquals(2, match.getBaseWord().getStartPosition());
  //    Assert.assertEquals(1, match.getWitnessWord().getStartPosition());
  //  }
  //
  //  @Ignore
  //  @Test
  //  public void testTransposition() {
  //    final Segment a = builder.build("a", "the black cat and the white cat").getFirstSegment();
  //    final Segment b = builder.build("b", "the white cat and the black cat").getFirstSegment();
  //    final SubsegmentExtractor sse = new SubsegmentExtractor(a, b);
  //    sse.go();
  //    final WitnessSegmentPhrases pa = sse.getWitnessSegmentPhrases("a");
  //    final WitnessSegmentPhrases pb = sse.getWitnessSegmentPhrases("b");
  //
  //    System.out.println(pa);
  //    Assert.assertEquals(3, pa.size());
  //    Assert.assertEquals(3, pb.size());
  //
  //    final Set<Match<Phrase>> matches = LeftToRightMatcher.match(pa, pb);
  //    Assert.assertEquals(3, matches.size());
  //    //    final Match<Phrase> match = matches.iterator().next();
  //    //    Assert.assertEquals(1, match.getBaseWord().getStartPosition());
  //    //    Assert.assertEquals(1, match.getWitnessWord().getStartPosition());
  //  }
  //
  //  private SubsegmentExtractor defaultSegmentExtractor() {
  //    final Segment a = builder.build("a", "Zijn hond liep aan zijn hand.").getFirstSegment();
  //    final Segment b = builder.build("b", "Op zijn pad liep zijn hond, aan zijn hand.").getFirstSegment();
  //    final Segment c = builder.build("c", "Met zijn hond aan zijn hand, liep hij op zijn pad.").getFirstSegment();
  //    final SubsegmentExtractor sse = new SubsegmentExtractor(a, b, c);
  //    return sse;
  //  }

}
