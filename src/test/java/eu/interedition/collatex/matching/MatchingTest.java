package eu.interedition.collatex.matching;

import java.util.Set;

import org.junit.Assert;
import org.junit.Ignore;
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
    PossibleMatches matches = matcher.match(a, b);
    Set<Match> exactMatches = matches.getFixedMatches();
    Match expected = new Match(a.getWordOnPosition(2), b.getWordOnPosition(6));
    System.out.println(exactMatches);
    Assert.assertTrue(exactMatches.contains(expected));
  }

  //  @Test
  //  @Ignore
  //  public void testPossibleMatches() {
  //    WitnessBuilder builder = new WitnessBuilder();
  //    Witness a = builder.build("zijn hond liep aan zijn hand");
  //    Witness b = builder.build("op zijn pad liep zijn hond aan zijn hand");
  //    Matcher matcher = new Matcher();
  //    Result result = matcher.match(a, b);
  //    Set<MatchGroup> possibleMatches = result.getPossibleMatches();
  //    Iterator<MatchGroup> iterator = possibleMatches.iterator();
  //    MatchGroup next = iterator.next();
  //    Assert.assertEquals("[(1->2), (1->5), (1->8)]", next.toString());
  //    System.out.println(next);
  //  }

  @Test
  public void testSelectBestMatchFromPossibleMatches() {
    WitnessBuilder builder = new WitnessBuilder();
    Witness a = builder.build("zijn hond liep aan zijn hand");
    Witness b = builder.build("op zijn pad liep zijn hond aan zijn hand");
    Matcher matcher = new Matcher();
    Permutation permutation = matcher.getBestPermutation(a, b);
    Set<Match> matches = permutation.getMatches();
    String expected = "[(2->6), (3->4), (4->7), (6->9), (1->5), (5->8)]";
    Assert.assertEquals(expected, matches.toString());
    Assert.assertEquals(1, permutation.getNonMatches(a, b).size());
    Assert.assertEquals(3, permutation.getMatchSequences().size());
  }

  // TODO: make this test work!
  @Ignore
  @Test
  public void testMatchingFromBtoA() {
    WitnessBuilder builder = new WitnessBuilder();
    Witness a = builder.build("op zijn pad liep zijn hond aan zijn hand");
    Witness b = builder.build("zijn hond liep aan zijn hand");
    Matcher matcher = new Matcher();
    Permutation permutation = matcher.getBestPermutation(a, b);
    Set<Match> matches = permutation.getMatches();
    String expected = "[(4->3), (6->2), (7->4), (9->6), (5->1), (8->5)]";
    Assert.assertEquals(expected, matches.toString());
    Assert.assertEquals(1, permutation.getNonMatches(a, b).size());
    Assert.assertEquals(3, permutation.getMatchSequences().size());
  }

}
