package eu.interedition.collatex.match;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.sd_editions.collatex.Block.Util;
import com.sd_editions.collatex.match.Subsegment;
import com.sd_editions.collatex.match.SubsegmentExtractor;
import com.sd_editions.collatex.match.Subsegments;

import eu.interedition.collatex.alignment.Phrase;
import eu.interedition.collatex.input.Segment;
import eu.interedition.collatex.input.builders.WitnessBuilder;

public class SubsegmentExtractorTest {
  private WitnessBuilder builder;

  @Before
  public void setup() {
    builder = new WitnessBuilder();
  }

  @Test
  public void testMatchingWordPositionsPerWitness() {
    Segment a = builder.build("a", "zijn hond liep aan zijn hand");
    Segment b = builder.build("b", "op zijn pad liep zijn hond aan zijn hand");
    Segment c = builder.build("c", "met zijn hond aan zijn hand liep hij op zijn pad");
    SubsegmentExtractor p2 = new SubsegmentExtractor(a, b, c);

    Subsegment zijnPositions = p2.matchingWordPositionsPerWitness("zijn");
    // all 3 witnesses have at least 1 'zijn':
    assertEquals(3, zijnPositions.size());

    assertContainsPositions(zijnPositions.get("a"), 1, 5);
    assertContainsPositions(zijnPositions.get("b"), 2, 5, 8);
    assertContainsPositions(zijnPositions.get("c"), 2, 5, 10);
  }

  private void assertContainsPositions(List<Integer> positionsA, int... positions) {
    for (int position : positions) {
      assertTrue("position " + position + " not found", positionsA.contains(Integer.valueOf(position)));
    }
  }

  @Test
  public void testGetOneWordSequences() {
    SubsegmentExtractor p2 = defaultSegmentExtractor();

    Subsegments oneWordSegments = p2.getOneWordSubsegments();
    Subsegment hondSequences = oneWordSegments.get("hond");
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
    SubsegmentExtractor sse = defaultSegmentExtractor();
    sse.go();
    assertNotNull(sse);
    Subsegments subsegments = sse.getSubsegments();
    assertNotNull(subsegments);
    Util.p(subsegments);
  }

  @Test
  public void testGetPhrasesPerSegment() {
    SubsegmentExtractor sse = defaultSegmentExtractor();
    sse.go();
    Map<String, List<Phrase>> phrasesPerSegment = sse.getPhrasesPerSegment();
    assertNotNull(phrasesPerSegment);
    assertEquals(3, phrasesPerSegment.size());
    Util.p(phrasesPerSegment);
  }

  private SubsegmentExtractor defaultSegmentExtractor() {
    Segment a = builder.build("a", "Zijn hond liep aan zijn hand.");
    Segment b = builder.build("b", "Op zijn pad liep zijn hond, aan zijn hand.");
    Segment c = builder.build("c", "Met zijn hond aan zijn hand, liep hij op zijn pad.");
    SubsegmentExtractor sse = new SubsegmentExtractor(a, b, c);
    return sse;
  }
}
