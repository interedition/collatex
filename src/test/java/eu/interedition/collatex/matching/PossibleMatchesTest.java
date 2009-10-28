package eu.interedition.collatex.matching;

import java.util.Collection;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import eu.interedition.collatex.alignment.Match;
import eu.interedition.collatex.alignment.UnfixedAlignment;
import eu.interedition.collatex.alignment.functions.Matcher;
import eu.interedition.collatex.input.Witness;
import eu.interedition.collatex.input.Word;
import eu.interedition.collatex.input.builders.WitnessBuilder;

// TODO: rename to unfixed alignment test!
public class PossibleMatchesTest {
  private WitnessBuilder builder;

  @Before
  public void setup() {
    builder = new WitnessBuilder();
  }

  @Test
  public void testExactMatchesAreFixed() {
    Witness a = builder.build("zijn hond liep aan zijn hand");
    Witness b = builder.build("op zijn pad liep zijn hond aan zijn hand");
    UnfixedAlignment unfixedAlignment = Matcher.createFirstUnfixedAlignment(a.getFirstSegment(), b.getFirstSegment());
    Set<Match> exactMatches = unfixedAlignment.getFixedMatches();
    String expected = "[(3->4), (4->7)]";
    Assert.assertEquals(expected, exactMatches.toString());
  }

  @Test
  public void testPossibleMatchesAsAMap() {
    Witness a = builder.build("zijn hond liep aan zijn hand");
    Witness b = builder.build("op zijn pad liep zijn hond aan zijn hand");
    UnfixedAlignment unfixedAlignment = Matcher.createFirstUnfixedAlignment(a.getFirstSegment(), b.getFirstSegment());
    Word zijn = a.getFirstSegment().getWordOnPosition(1);
    Collection<Match> linked = unfixedAlignment.getMatchesThatLinkFrom(zijn);
    Assert.assertEquals("[(1->2), (1->5), (1->8)]", linked.toString());

    Word zijnB = b.getFirstSegment().getWordOnPosition(2);
    Collection<Match> links = unfixedAlignment.getMatchesThatLinkTo(zijnB);
    Assert.assertEquals("[(1->2), (5->2)]", links.toString());
  }
}
