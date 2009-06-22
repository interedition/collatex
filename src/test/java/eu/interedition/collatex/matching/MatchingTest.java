package eu.interedition.collatex.matching;

import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import eu.interedition.collatex.collation.Match;
import eu.interedition.collatex.input.Witness;
import eu.interedition.collatex.input.WitnessBuilder;

public class MatchingTest {

  @Test
  public void testExactMatches() {
    WitnessBuilder builder = new WitnessBuilder();
    Witness a = builder.build("zijn hond liep aan zijn hand");
    Witness b = builder.build("op zijn pad liep zijn hond aan zijn hand");
    Matcher matcher = new Matcher();
    Alignment matches = matcher.align(a, b);
    Set<Match> exactMatches = matches.getFixedMatches();
    String expected = "[(3->4), (4->7)]";
    Assert.assertEquals(expected, exactMatches.toString());
  }

  @Test
  // TODO: test near matches separate?
  public void testNearMatch() {
    WitnessBuilder builder = new WitnessBuilder();
    Witness a = builder.build("a near match");
    Witness b = builder.build("a nar match");
    Matcher matcher = new Matcher();
    Alignment matches = matcher.align(a, b);
    Set<Match> fixedMatches = matches.getFixedMatches();
    Assert.assertEquals("[(1->1), (2->2), (3->3)]", fixedMatches.toString());
  }

  @Test
  public void testNoPermutationsOnlyExactMatches() {
    WitnessBuilder builder = new WitnessBuilder();
    Witness a = builder.build("deze zinnen zijn hetzelfde");
    Witness b = builder.build("deze zinnen zijn hetzelfde met een aanvulling");
    Matcher matcher = new Matcher();
    Collation collation = matcher.collate(a, b);
    Set<Match> matches = collation.getMatches();
    String expected = "[(1->1), (2->2), (3->3), (4->4)]";
    Assert.assertEquals(expected, matches.toString());
  }

  @Test
  public void testSelectBestMatchFromPossibleMatches() {
    WitnessBuilder builder = new WitnessBuilder();
    Witness a = builder.build("zijn hond liep aan zijn hand");
    Witness b = builder.build("op zijn pad liep zijn hond aan zijn hand");
    Matcher matcher = new Matcher();
    Collation collation = matcher.collate(a, b);
    Set<Match> matches = collation.getMatches();
    String expected = "[(3->4), (4->7), (2->6), (6->9), (1->5), (5->8)]";
    Assert.assertEquals(expected, matches.toString());
    Assert.assertEquals(1, collation.getNonMatches().size());
    Assert.assertEquals(3, collation.getMatchSequences().size());
  }

  @Test
  public void testTreeTimesZijnAlsoWorks() {
    WitnessBuilder builder = new WitnessBuilder();
    Witness a = builder.build("zijn hond liep aan zijn hand op zijn dag");
    Witness b = builder.build("op zijn pad liep zijn hond aan zijn hand op zijn dag");
    Matcher matcher = new Matcher();
    Collation collation = matcher.collate(a, b);
    Set<Match> matches = collation.getMatches();
    String expected = "[(3->4), (4->7), (9->12), (2->6), (6->9), (1->5), (5->8), (7->10), (8->11)]";
    Assert.assertEquals(expected, matches.toString());
    Assert.assertEquals(1, collation.getNonMatches().size());
    Assert.assertEquals(3, collation.getMatchSequences().size());
  }

  @Test
  public void testMatchingFromBtoA() {
    WitnessBuilder builder = new WitnessBuilder();
    Witness a = builder.build("op zijn pad liep zijn hond aan zijn hand");
    Witness b = builder.build("zijn hond liep aan zijn hand");
    Matcher matcher = new Matcher();
    Collation collation = matcher.collate(a, b);
    Set<Match> matches = collation.getMatches();
    String expected = "[(4->3), (7->4), (6->2), (9->6), (5->1), (8->5)]";
    Assert.assertEquals(expected, matches.toString());
    Assert.assertEquals(1, collation.getNonMatches().size());
    Assert.assertEquals(3, collation.getMatchSequences().size());
  }

}
