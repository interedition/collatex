package eu.interedition.collatex.matching;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Sets;

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

  @Test
  public void testExtractSegments2() {
    Witness a = builder.build("zijn hond liep aan zijn hand");
    Witness b = builder.build("op zijn pad liep zijn hond aan zijn hand");
    Set<WordSegment> segments = SegmentExtractor.extractSegmentSet(a, b);
    assertTrue("(some) expected segments are missing", segmentSetContains(segments, "zijn hond", "aan zijn hand"));
  }

  @Test
  public void testExtractSegments3() {
    Witness a = builder.build("zijn hond liep aan zijn hand");
    Witness b = builder.build("op zijn pad liep zijn hond aan zijn hand");
    Witness c = builder.build("met zijn hond aan zijn hand liep hij op zijn pad");
    Set<WordSegment> segments = SegmentExtractor.extractSegmentSet(a, b, c);
    assertFalse(segments.isEmpty());
    assertTrue("(some) expected segments are missing", segmentSetContains(segments, "zijn hond", "aan zijn hand", "op zijn pad"));
  }

  private boolean segmentSetContains(Set<WordSegment> segmentSet, String... segments) {
    Set<String> containedStrings = Sets.newHashSet();
    for (WordSegment wordsegment : segmentSet) {
      containedStrings.add(wordsegment.toString());
    }
    List<String> expectedSegmentList = Arrays.asList(segments);
    //    Util.p(containedStrings);
    if (containedStrings.size() > expectedSegmentList.size()) {
      fail("More segments than expected:");
    }
    if (containedStrings.size() < expectedSegmentList.size()) {
      fail("Less segments than expected");
    }
    return containedStrings.containsAll(expectedSegmentList);
  }
}
