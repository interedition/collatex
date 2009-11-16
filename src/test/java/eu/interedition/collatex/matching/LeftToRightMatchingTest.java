package eu.interedition.collatex.matching;

import java.util.Iterator;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.sd_editions.collatex.match.SubsegmentExtractor;

import eu.interedition.collatex.alignment.Match;
import eu.interedition.collatex.input.Phrase;
import eu.interedition.collatex.input.Segment;
import eu.interedition.collatex.input.WitnessSegmentPhrases;
import eu.interedition.collatex.input.builders.WitnessBuilder;
import eu.interedition.collatex.match.Matcher;

public class LeftToRightMatchingTest {
  private WitnessBuilder builder;

  @Before
  public void setup() {
    builder = new WitnessBuilder();
  }

  @Test
  public void testMatching() {
    final Segment a = builder.build("a", "everything matches").getFirstSegment();
    final Segment b = builder.build("b", "everything matches").getFirstSegment();
    final SubsegmentExtractor sse = new SubsegmentExtractor(a, b);
    sse.go();
    final WitnessSegmentPhrases pa = sse.getWitnessSegmentPhrases("a");
    final WitnessSegmentPhrases pb = sse.getWitnessSegmentPhrases("b");

    Assert.assertEquals(1, pa.size());
    Assert.assertEquals(1, pb.size());

    final Set<Match<Phrase>> matches = Matcher.match(pa, pb);
    Assert.assertEquals(1, matches.size());
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

    final Set<Match<Phrase>> matches = Matcher.match(pa, pb);
    Assert.assertEquals(1, matches.size());
    final Match<Phrase> match = matches.iterator().next();
    Assert.assertEquals(1, match.getBaseWord().getStartPosition());
    Assert.assertEquals(2, match.getWitnessWord().getStartPosition());
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

    final Set<Match<Phrase>> matches = Matcher.match(pa, pb);
    Assert.assertEquals(2, matches.size());

    final Iterator<Match<Phrase>> i = matches.iterator();
    final Match<Phrase> match = i.next();
    Assert.assertEquals(1, match.getBaseWord().getStartPosition());
    Assert.assertEquals(1, match.getWitnessWord().getStartPosition());

    final Match<Phrase> match2 = i.next();
    Assert.assertEquals(2, match2.getBaseWord().getStartPosition());
    Assert.assertEquals(3, match2.getWitnessWord().getStartPosition());
  }

  @Test
  public void testAdditionAtTheEnd() {
    final Segment a = builder.build("a", "everything matches").getFirstSegment();
    final Segment b = builder.build("b", "everything matches addition").getFirstSegment();
    final SubsegmentExtractor sse = new SubsegmentExtractor(a, b);
    sse.go();
    final WitnessSegmentPhrases pa = sse.getWitnessSegmentPhrases("a");
    final WitnessSegmentPhrases pb = sse.getWitnessSegmentPhrases("b");

    Assert.assertEquals(1, pa.size());
    Assert.assertEquals(2, pb.size());

    final Set<Match<Phrase>> matches = Matcher.match(pa, pb);
    Assert.assertEquals(1, matches.size());
    final Match<Phrase> match = matches.iterator().next();
    Assert.assertEquals(1, match.getBaseWord().getStartPosition());
    Assert.assertEquals(1, match.getWitnessWord().getStartPosition());
  }

  @Test
  public void testOmittedInFront() {
    final Segment a = builder.build("a", "omitted everything matches").getFirstSegment();
    final Segment b = builder.build("b", "everything matches").getFirstSegment();
    final SubsegmentExtractor sse = new SubsegmentExtractor(a, b);
    sse.go();
    final WitnessSegmentPhrases pa = sse.getWitnessSegmentPhrases("a");
    final WitnessSegmentPhrases pb = sse.getWitnessSegmentPhrases("b");

    Assert.assertEquals(2, pa.size());
    Assert.assertEquals(1, pb.size());

    final Set<Match<Phrase>> matches = Matcher.match(pa, pb);
    Assert.assertEquals(1, matches.size());
    final Match<Phrase> match = matches.iterator().next();
    Assert.assertEquals(2, match.getBaseWord().getStartPosition());
    Assert.assertEquals(1, match.getWitnessWord().getStartPosition());
  }

  @Ignore
  @Test
  public void testTransposition() {
    final Segment a = builder.build("a", "the black cat and the white cat").getFirstSegment();
    final Segment b = builder.build("b", "the white cat and the black cat").getFirstSegment();
    final SubsegmentExtractor sse = new SubsegmentExtractor(a, b);
    sse.go();
    final WitnessSegmentPhrases pa = sse.getWitnessSegmentPhrases("a");
    final WitnessSegmentPhrases pb = sse.getWitnessSegmentPhrases("b");

    System.out.println(pa);
    Assert.assertEquals(3, pa.size());
    Assert.assertEquals(3, pb.size());

    final Set<Match<Phrase>> matches = Matcher.match(pa, pb);
    Assert.assertEquals(3, matches.size());
    //    final Match<Phrase> match = matches.iterator().next();
    //    Assert.assertEquals(1, match.getBaseWord().getStartPosition());
    //    Assert.assertEquals(1, match.getWitnessWord().getStartPosition());
  }

  private SubsegmentExtractor defaultSegmentExtractor() {
    final Segment a = builder.build("a", "Zijn hond liep aan zijn hand.").getFirstSegment();
    final Segment b = builder.build("b", "Op zijn pad liep zijn hond, aan zijn hand.").getFirstSegment();
    final Segment c = builder.build("c", "Met zijn hond aan zijn hand, liep hij op zijn pad.").getFirstSegment();
    final SubsegmentExtractor sse = new SubsegmentExtractor(a, b, c);
    return sse;
  }

}
