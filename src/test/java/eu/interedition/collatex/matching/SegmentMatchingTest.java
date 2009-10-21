package eu.interedition.collatex.matching;

import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Before;

import com.google.common.collect.Sets;
import com.sd_editions.collatex.Block.Util;

import eu.interedition.collatex.input.Witness;
import eu.interedition.collatex.input.builders.WitnessBuilder;
import eu.interedition.collatex.match.SegmentExtractor;
import eu.interedition.collatex.match.WordSegment;

public class SegmentMatchingTest {

  private WitnessBuilder builder;

  @Before
  public void setUp() {
    builder = new WitnessBuilder();
  }

  @After
  public void tearDown() {
    System.out.println();
  }

  //  @Test
  public void testExtractSegments1() {
    Witness a = builder.build("a", "zijn hond liep aan zijn hand");
    Witness b = builder.build("b", "op zijn pad liep zijn hond aan zijn hand");

    List<WordSegment> segments = SegmentExtractor.extractSegments(a, b);
    assert1(segments);

    segments = SegmentExtractor.extractSegments(b, a);
    assert1(segments);
  }

  private void assert1(List<WordSegment> segments) {
    assertTrue("(some) expected segments are missing", segmentListContains(segments, //
        "zijn hond", "liep", "aan zijn hand", "op zijn pad"));
  }

  //  @Test
  public void testExtractSegments2() {
    Witness a = builder.build("a", "zijn hond liep aan zijn hand op zijn dag");
    Witness b = builder.build("b", "op zijn pad liep zijn hond aan zijn hand op zijn dag");

    List<WordSegment> segments = SegmentExtractor.extractSegments(a, b);
    assert2(segments);

    segments = SegmentExtractor.extractSegments(b, a);
    assert2(segments);
  }

  private void assert2(List<WordSegment> segments) {
    assertTrue("(some) expected segments are missing", segmentListContains(segments, //
        "zijn hond", "liep", "aan zijn hand op zijn dag", "op zijn pad"));
  }

  //  @Test
  public void testExtractSegments3() {
    Witness a = builder.build("a", "zijn hond liep aan zijn hand");
    Witness b = builder.build("b", "op zijn pad liep zijn hond aan zijn hand");
    Witness c = builder.build("c", "met zijn hond aan zijn hand liep hij op zijn pad");

    List<WordSegment> segments = SegmentExtractor.extractSegments(a, b, c);
    assert3(segments);

    segments = SegmentExtractor.extractSegments(b, c, a);
    assert3(segments);

    segments = SegmentExtractor.extractSegments(c, a, b);
    assert3(segments);

    segments = SegmentExtractor.extractSegments(a, c, b);
    assert3(segments);
  }

  private void assert3(List<WordSegment> segments) {
    assertTrue("(some) expected segments are missing", segmentListContains(segments, //
        "zijn hond", "liep", "aan zijn hand", "op zijn pad", "met", "hij"));
  }

  private boolean segmentListContains(List<WordSegment> segmentSet, String... segments) {
    Set<String> containedStrings = Sets.newHashSet();
    for (WordSegment wordsegment : segmentSet) {
      containedStrings.add(wordsegment.toString());
    }
    List<String> expectedSegmentList = Arrays.asList(segments);
    Util.p(containedStrings);
    if (containedStrings.size() > expectedSegmentList.size()) {
      fail("More segments than expected:");
    } else if (containedStrings.size() < expectedSegmentList.size()) {
      fail("Less segments than expected");
    }
    return containedStrings.containsAll(expectedSegmentList);
  }
}
