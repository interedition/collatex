package eu.interedition.collatex.matching;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import com.sd_editions.collatex.permutations.MatchGroup;

import eu.interedition.collatex.collation.Match;
import eu.interedition.collatex.input.Witness;
import eu.interedition.collatex.input.WitnessBuilder;

public class MatchingTest {
  @Test
  public void testExactMatchesAndPossibleMatches() {
    WitnessBuilder builder = new WitnessBuilder();
    Witness a = builder.build("zijn hond liep aan zijn hand");
    Witness b = builder.build("op zijn pad liep zijn hond aan zijn hand");
    Matcher matcher = new Matcher();
    Result result = matcher.match(a, b);
    Set<Match> exactMatches = result.getExactMatches();
    Match expected = new Match(a.getWordOnPosition(2), b.getWordOnPosition(6));
    System.out.println(exactMatches);
    Assert.assertTrue(exactMatches.contains(expected));
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
    List<Permutation> permutations = matcher.getPossiblePermutationsForMatchGroup(a, b, 1);

    //selectBestPossiblePermutation(a, b, permutations);
  }

}
