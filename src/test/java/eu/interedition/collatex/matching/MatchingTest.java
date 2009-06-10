package eu.interedition.collatex.matching;

import java.util.Iterator;
import java.util.Set;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import com.sd_editions.collatex.permutations.MatchGroup;

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
    Result result = matcher.match(a, b);
    Set<Match> exactMatches = result.getExactMatches();
    Match expected = new Match(a.getWordOnPosition(2), b.getWordOnPosition(6));
    System.out.println(exactMatches);
    Assert.assertTrue(exactMatches.contains(expected));
  }

  @Test
  @Ignore
  public void testPossibleMatches() {
    WitnessBuilder builder = new WitnessBuilder();
    Witness a = builder.build("zijn hond liep aan zijn hand");
    Witness b = builder.build("op zijn pad liep zijn hond aan zijn hand");
    Matcher matcher = new Matcher();
    Result result = matcher.match(a, b);
    Set<MatchGroup> possibleMatches = result.getPossibleMatches();
    Iterator<MatchGroup> iterator = possibleMatches.iterator();
    MatchGroup next = iterator.next();
    Assert.assertEquals("[(1->2), (1->5), (1->8)]", next.toString());
    System.out.println(next);
  }

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

  // TODO: make test work; remove ignore annotation!
  @Test
  @Ignore
  public void testPossibleMatchesAsAMap() {
    WitnessBuilder builder = new WitnessBuilder();
    Witness a = builder.build("zijn hond liep aan zijn hand");
    Witness b = builder.build("op zijn pad liep zijn hond aan zijn hand");
    Matcher matcher = new Matcher();
    Result result = matcher.match(a, b);
    // NOTE: this is already done in the exact matches test!
    // Note: this exact matches test could be fleshed out more!
    //    Set<Match> exactMatches = result.getExactMatches();
    //    Match expected = new Match(a.getWordOnPosition(2), b.getWordOnPosition(6));
    //    System.out.println(exactMatches);
    //    Assert.assertTrue(exactMatches.contains(expected));

    MatchGroup group = result.getMatchGroupForBaseWord(1);
    Assert.assertEquals("[(1->2), (1->5), (1->8)]", group.toString());

    MatchGroup group2 = result.getMatchGroupForWitnessWord(2);
    Assert.assertEquals("[(2->1), (2->5)", group2.toString());

    // Note: this is the old possibleMatches test!
    //    Set<MatchGroup> possibleMatches = result.getPossibleMatches();
    //    Iterator<MatchGroup> iterator = possibleMatches.iterator();
    //    MatchGroup next = iterator.next();
    //    Assert.assertEquals("[(1->2), (1->5), (1->8)]", next.toString());
    //    System.out.println(next);
  }
}
