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
    Segment a = builder.build("a", "Zijn hond liep aan zijn hand.");
    Segment b = builder.build("b", "Op zijn pad liep zijn hond, aan zijn hand.");
    Segment c = builder.build("c", "Met zijn hond aan zijn hand, liep hij op zijn pad.");
    SubsegmentExtractor p2 = new SubsegmentExtractor(a, b, c);

    Subsegments oneWordSegments = p2.getOneWordSubsegments();
    Subsegment hondSequences = oneWordSegments.get("hond");
    assertContainsPositions(hondSequences.get("a"), 2);
    // nr. of unique normalized words in all witnesses combined
    assertEquals(9, oneWordSegments.size());

    // sequences: "zijn", "hond", "liep", "aan", "hand", "op", "pad", "met", "hij"

    // neem: zijn
    // zijn is in witness a,b,c
    // nextwords_for_zijn: hond, hand,pad
    //  hond in a,b,c => "zijn hond" is een sequence
    //  hand in a,b,c => "zijn hand" is een sequence
    //  pad in b,c => "zijn pad" is een sequence
    // verwijder zijn, hond, hand en pad als sequences
    // voeg toe: "zijn hond", "zijn hand", "zijn pad"

    // sequences: "zijn hond", "liep", "aan", "zijn hand", "op", "zijn pad", "met", "hij"

    // groei "zijn hond": a:liep,b:aan,c:aan => sequence is final
    // groei "zijn hand": a:-,b:-,c:liep => final
    // groei "zijn pad": b:liep,c:- => final

    // sequences: "zijn hond"!, "liep", "aan", "zijn hand"!, "op", "zijn pad"!, "met", "hij"

    // neem "liep"
    // in witness a,b,c
    // nextwords": aan,zijn,hij
    // nextwords.size=(a,b,c).size => liep is final

    // sequences: "zijn hond"!, "liep"!, "aan", "zijn hand"!, "op", "zijn pad"!, "met", "hij"

    // neem "aan"
    // in witness a,b,c
    // nextwords: zijn (onderdeel van "zijn hand"
    // nextwords.size=(a,b,c).size => "aan zijn hand" is nieuwe sequence
    // verwijder "aan" en "zijn hand"
    // voeg "aan zijn hand" toe. "zijn hand" was final, dus is "aan zijn hand" dat ook

    // sequences: "zijn hond"!, "liep"!, "aan zijn hand"!, "op", "zijn pad"!, "met", "hij"

    // "op"
    // in witness b,c
    // nextwords: zijn (onderdeel van "zijn pad")
    // nextwords.size=(b,c).size => "op zijn pad" is nieuwe sequence
    // verwijder "op" en "zijn pad"
    // voeg "op zijn pad" toe. "zijn pad" was final, dus is "op zijn pad" dat ook

    // sequences: "zijn hond"!, "liep"!, "aan zijn hand"!, "op zijn pad"!, "met", "hij"

    // "met"
    // in witness c
    // nextword: zijn (onderdeel van "zijn hond", dat andere witnesses heeft dan met => met is final) 

    // sequences: "zijn hond"!, "liep"!, "aan zijn hand"!, "op zijn pad"!, "met"!, "hij"

    // "hij"
    // in witness c
    // nextword: op (onderdeel van "op zijn pad", dat andere witnesses heeft dan met => hij is final)

    // sequences: "zijn hond"!, "liep"!, "aan zijn hand"!, "op zijn pad"!, "met"!, "hij"!

    // geen woorden meer: klaar!

    // sequences: "zijn hond (a1-2,b)"!, "liep"!, "aan zijn hand"!, "op zijn pad"!, "met"!, "hij"!

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
    Segment a = builder.build("a", "Zijn hond liep aan zijn hand.");
    Segment b = builder.build("b", "Op zijn pad liep zijn hond, aan zijn hand.");
    Segment c = builder.build("c", "Met zijn hond aan zijn hand, liep hij op zijn pad.");
    SubsegmentExtractor sse = new SubsegmentExtractor(a, b, c);
    sse.go();
    assertNotNull(sse);
    Subsegments subsegments = sse.getSubsegments();
    assertNotNull(subsegments);
    Util.p(subsegments);
  }

  @Test
  public void testGetPhrasesPerSegment() {
    Segment a = builder.build("a", "Zijn hond liep aan zijn hand.");
    Segment b = builder.build("b", "Op zijn pad liep zijn hond, aan zijn hand.");
    Segment c = builder.build("c", "Met zijn hond aan zijn hand, liep hij op zijn pad.");
    SubsegmentExtractor sse = new SubsegmentExtractor(a, b, c);
    sse.go();
    Map<String, List<Phrase>> phrasesPerSegment = sse.getPhrasesPerSegment();
    assertNotNull(phrasesPerSegment);
    assertEquals(3, phrasesPerSegment.size());
    Util.p(phrasesPerSegment);
  }
}
