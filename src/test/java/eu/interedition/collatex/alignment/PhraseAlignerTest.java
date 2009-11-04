package eu.interedition.collatex.alignment;

import java.util.Set;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Sets;

import eu.interedition.collatex.alignment.functions.PhraseAligner;
import eu.interedition.collatex.input.Phrase;
import eu.interedition.collatex.input.WitnessSegmentPhrases;
import eu.interedition.collatex.input.builders.SegmentBuilder;
import eu.interedition.collatex.input.builders.WitnessBuilder;

public class PhraseAlignerTest {

  private WitnessBuilder builder;

  @Before
  public void setUp() {
    builder = new WitnessBuilder();
  }

  @Test
  public void testNoPermutationsOnlyExactMatches() {
    // THIS IS ALL SETUP!
    WitnessSegmentPhrases a = SegmentBuilder.build("A", "deze zinnen zijn hetzelfde");
    WitnessSegmentPhrases b = SegmentBuilder.build("B", "deze zinnen zijn hetzelfde");
    Phrase pa = a.getPhraseOnPosition(1);
    Phrase pb = b.getPhraseOnPosition(1);
    Match<Phrase> match = new Match<Phrase>(pa, pb);
    Set<Match<Phrase>> exactMatches = Sets.newHashSet(match);
    Set<Match<Phrase>> nonExactMatches = Sets.emptySortedSet();
    UnfixedAlignment<Phrase> u = new UnfixedAlignment<Phrase>(exactMatches, nonExactMatches);

    // DO THE ACTUAL ALIGNMENT
    Alignment<Phrase> alignment = PhraseAligner.align(a, b, u);
    Set<Match<Phrase>> matches = alignment.getMatches();
    String expected = "[(1->1)]";
    Assert.assertEquals(expected, matches.toString());
    Assert.assertTrue(alignment.getGaps().isEmpty());
  }
}
