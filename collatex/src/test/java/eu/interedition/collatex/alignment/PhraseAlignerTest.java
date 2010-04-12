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
    final WitnessSegmentPhrases a = SegmentBuilder.build("A", "deze zinnen zijn hetzelfde");
    final WitnessSegmentPhrases b = SegmentBuilder.build("B", "deze zinnen zijn hetzelfde");
    final Phrase pa = a.getPhraseOnPosition(1);
    final Phrase pb = b.getPhraseOnPosition(1);
    final Match<Phrase> match = new Match<Phrase>(pa, pb);
    final Set<Match<Phrase>> exactMatches = Sets.newHashSet(match);
    final Set<Match<Phrase>> nonExactMatches = Sets.emptySortedSet();
    final UnfixedAlignment<Phrase> u = new UnfixedAlignment<Phrase>(exactMatches, nonExactMatches);
    // TODO code to make a,b,u

    // DO THE ACTUAL ALIGNMENT
    final Alignment<Phrase> alignment = PhraseAligner.align(a, b, u);
    final Set<Match<Phrase>> matches = alignment.getMatches();
    final String expected = "[(1->1)]";
    Assert.assertEquals(expected, matches.toString());
    Assert.assertTrue(alignment.getGaps().isEmpty());
  }
}
