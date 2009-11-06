package eu.interedition.collatex.match;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.sd_editions.collatex.Block.Util;
import com.sd_editions.collatex.match.Subsegment;
import com.sd_editions.collatex.match.SubsegmentExtractor;
import com.sd_editions.collatex.match.Subsegments;

import eu.interedition.collatex.alignment.Match;
import eu.interedition.collatex.alignment.UnfixedAlignment;
import eu.interedition.collatex.input.Phrase;
import eu.interedition.collatex.input.Segment;
import eu.interedition.collatex.input.WitnessSegmentPhrases;
import eu.interedition.collatex.input.builders.WitnessBuilder;

public class SubsegmentExtractorTest {
  private WitnessBuilder builder;

  @Before
  public void setup() {
    builder = new WitnessBuilder();
  }

  @Test
  public void testMatchingWordPositionsPerWitness() {
    final SubsegmentExtractor p2 = defaultSegmentExtractor();

    final Subsegment zijnPositions = p2.matchingWordPositionsPerWitness("zijn");
    assertEquals(3, zijnPositions.size());

    assertContainsPositions(zijnPositions.get("a"), 1, 5);
    assertContainsPositions(zijnPositions.get("b"), 2, 5, 8);
    assertContainsPositions(zijnPositions.get("c"), 2, 5, 10);
  }

  private void assertContainsPositions(final List<Integer> positionsA, final int... positions) {
    for (final int position : positions) {
      assertTrue("position " + position + " not found", positionsA.contains(Integer.valueOf(position)));
    }
  }

  @Test
  public void testGetOneWordSequences() {
    final SubsegmentExtractor p2 = defaultSegmentExtractor();

    final Subsegments oneWordSegments = p2.getOneWordSubsegments();
    Util.p(oneWordSegments);
    final Subsegment hondSequences = oneWordSegments.get("hond");
    Util.p(hondSequences);
    assertContainsPositions(hondSequences.get("a"), 2);
    // nr. of unique normalized words in all witnesses combined
    assertEquals(9, oneWordSegments.size());

    //    Set<String> hondInWitnesses = hondSequences.keySet();
    //    Set<Entry<String, List<Integer>>> entrySet = hondSequences.entrySet();
    //    Set<String> wordsAfterHond = Sets.newHashSet();
    //    for (Entry<String, List<Integer>> entry : entrySet) {
    //      
    //      
    //    }

    //    ArrayList<String> newArrayList = Lists.newArrayList(oneWordSequences.keySet());
    //    Collections.sort(newArrayList);
    //    Util.p(newArrayList);
  }

  @Test
  public void testGo() {
    final SubsegmentExtractor sse = defaultSegmentExtractor();
    sse.go();
    assertNotNull(sse);
    final Subsegments subsegments = sse.getSubsegments();
    assertNotNull(subsegments);
    Util.p(subsegments);
  }

  @Ignore
  @Test
  public void testGetPhrasesPerSegment() {
    final SubsegmentExtractor sse = defaultSegmentExtractor();
    sse.go();
    assertPhrasesFound(sse.getWitnessSegmentPhrases("a"), "'Zijn hond','liep','aan zijn hand.'");
    assertPhrasesFound(sse.getWitnessSegmentPhrases("b"), "'Op zijn pad','liep','zijn hond','aan zijn hand.'");
    assertPhrasesFound(sse.getWitnessSegmentPhrases("c"), "'Met','zijn hond','aan zijn hand,','liep','hij','op zijn pad.'");
  }

  private void assertPhrasesFound(final WitnessSegmentPhrases wsp, final String expectedPhrases) {
    assertNotNull(wsp);
    final String string = wsp.toString();
    assertTrue(expectedPhrases + " not found in " + string, string.contains(expectedPhrases));
    Util.p(wsp);
  }

  @Ignore
  @Test
  public void testGetUnfixedAlignment() {
    final SubsegmentExtractor sse = defaultSegmentExtractor();
    sse.go();
    final UnfixedAlignment<Phrase> unfixedAlignment = sse.getUnfixedAlignment();
    assertNotNull(unfixedAlignment);
    final Set<Match<Phrase>> fixedMatches = unfixedAlignment.getFixedMatches();
    // verwachte fixedMatches: 'zijn hond', 'liep', 'aan zijn hand', 'op zijn pad'
    assertEquals(4, fixedMatches.size());
    Util.p(fixedMatches);
    final Set<Match<Phrase>> unfixedMatches = unfixedAlignment.getUnfixedMatches();
    // geen unfixedMatches
    assertEquals(0, unfixedMatches.size());
    Util.p(unfixedMatches);
  }

  private SubsegmentExtractor defaultSegmentExtractor() {
    final Segment a = builder.build("a", "Zijn hond liep aan zijn hand.").getFirstSegment();
    final Segment b = builder.build("b", "Op zijn pad liep zijn hond, aan zijn hand.").getFirstSegment();
    final Segment c = builder.build("c", "Met zijn hond aan zijn hand, liep hij op zijn pad.").getFirstSegment();
    final SubsegmentExtractor sse = new SubsegmentExtractor(a, b, c);
    return sse;
  }
}
