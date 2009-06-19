package eu.interedition.collatex.matching;

import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import eu.interedition.collatex.collation.Match;
import eu.interedition.collatex.input.Witness;
import eu.interedition.collatex.input.WitnessBuilder;

public class MatchingTest {

  @Test
  // Note: this exact matches test could be fleshed out more!
  public void testExactMatches() {
    WitnessBuilder builder = new WitnessBuilder();
    Witness a = builder.build("zijn hond liep aan zijn hand");
    Witness b = builder.build("op zijn pad liep zijn hond aan zijn hand");
    Matcher matcher = new Matcher();
    Alignment matches = matcher.match(a, b);
    Set<Match> exactMatches = matches.getFixedMatches();
    Match expected = new Match(a.getWordOnPosition(2), b.getWordOnPosition(6));
    System.out.println(exactMatches);
    Assert.assertTrue(exactMatches.contains(expected));
  }

  @Test
  public void testNoPermutationsOnlyExactMatches() {
    WitnessBuilder builder = new WitnessBuilder();
    Witness a = builder.build("deze zinnen zijn hetzelfde");
    Witness b = builder.build("deze zinnen zijn hetzelfde met een aanvulling");
    Matcher matcher = new Matcher();
    Collation collation = matcher.getBestPermutation(a, b);
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
    Collation collation = matcher.getBestPermutation(a, b);
    Set<Match> matches = collation.getMatches();
    String expected = "[(2->6), (3->4), (4->7), (6->9), (1->5), (5->8)]";
    Assert.assertEquals(expected, matches.toString());
    Assert.assertEquals(1, collation.getNonMatches(a, b).size());
    Assert.assertEquals(3, collation.getMatchSequences().size());
  }

  @Test
  public void testTreeTimesZijnAlsoWorks() {
    WitnessBuilder builder = new WitnessBuilder();
    Witness a = builder.build("zijn hond liep aan zijn hand op zijn dag");
    Witness b = builder.build("op zijn pad liep zijn hond aan zijn hand op zijn dag");
    Matcher matcher = new Matcher();
    Collation collation = matcher.getBestPermutation(a, b);
    Set<Match> matches = collation.getMatches();
    String expected = "[(2->6), (3->4), (4->7), (6->9), (9->12), (1->5), (5->8), (7->10), (8->11)]";
    Assert.assertEquals(expected, matches.toString());
    Assert.assertEquals(1, collation.getNonMatches(a, b).size());
    Assert.assertEquals(3, collation.getMatchSequences().size());
  }

  @Test
  public void testMatchingFromBtoA() {
    WitnessBuilder builder = new WitnessBuilder();
    Witness a = builder.build("op zijn pad liep zijn hond aan zijn hand");
    Witness b = builder.build("zijn hond liep aan zijn hand");
    Matcher matcher = new Matcher();
    Collation collation = matcher.getBestPermutation(a, b);
    Set<Match> matches = collation.getMatches();
    String expected = "[(4->3), (6->2), (7->4), (9->6), (5->1), (8->5)]";
    Assert.assertEquals(expected, matches.toString());
    Assert.assertEquals(1, collation.getNonMatches(a, b).size());
    Assert.assertEquals(3, collation.getMatchSequences().size());
  }

}
