package com.sd_editions.collatex.permutations;

import java.util.List;

import junit.framework.TestCase;

import org.junit.Test;

import com.google.common.base.Join;
import com.google.common.collect.Lists;

import eu.interedition.collatex.input.WitnessBuilder;

public class CollateCoreTest extends TestCase {
  private WitnessBuilder builder;

  @Override
  protected void setUp() throws Exception {
    builder = new WitnessBuilder();
    super.setUp();
  }

  @Test
  public void testGetAllMatchNonMatchPermutations() {
    CollateCore cc = new CollateCore(builder.buildWitnesses("The black car", "The dark car", "the dark day"));
    List<List<MatchNonMatch>> allMatchNonMatchPermutations = cc.getAllMatchNonMatchPermutations();
    // there should be 3 witness pairs
    assertEquals(3, allMatchNonMatchPermutations.size());
    List<MatchNonMatch> list = allMatchNonMatchPermutations.get(0);
    assertEquals(1, list.size());
    showAllMatchNonMatchPermutations(allMatchNonMatchPermutations);
  }

  @Test
  public void testGetAllMatchNonMatchPermutations1() {
    CollateCore cc = new CollateCore(builder.buildWitnesses("The cat chases the dog and mouse", "The dog chases the cat and mouse", "the dog and mouse chase the cat"));
    List<List<MatchNonMatch>> allMatchNonMatchPermutations = cc.getAllMatchNonMatchPermutations();
    showAllMatchNonMatchPermutations(allMatchNonMatchPermutations);
    assertEquals(3, allMatchNonMatchPermutations.size());
  }

  void showAllMatchNonMatchPermutations(List<List<MatchNonMatch>> allMatchNonMatchPermutations) {
    for (List<MatchNonMatch> list : allMatchNonMatchPermutations) {
      println("witness pair " + allMatchNonMatchPermutations.indexOf(list));
      for (MatchNonMatch matchNonMatch : list) {
        println("  matchNonMatch " + list.indexOf(matchNonMatch));
        List<MatchSequence> matchSequencesForBase = matchNonMatch.getMatchSequencesForBase();
        List<NonMatch> nonMatches = matchNonMatch.getNonMatches();
        for (MatchSequence matchSequence : matchSequencesForBase) {
          println("    matchSequenceForBase " + matchSequencesForBase.indexOf(matchSequence));
          List<Match> matches = matchSequence.getMatches();
          List<String> baseWords = Lists.newArrayList();
          List<String> witnessWords = Lists.newArrayList();
          for (Match match : matches) {
            baseWords.add(match.getBaseWord().original);
            witnessWords.add(match.getWitnessWord().original);
          }
          println("      '" + Join.join(" ", baseWords) + "' -> '" + Join.join(" ", witnessWords) + "'");
        }
      }
    }
  }

  private void print(String string) {
    System.out.print(string);
  }

  private void println(String string) {
    System.out.println(string);
  }

  @Test
  public void testSortByVariation() {
    CollateCore collateCore = new CollateCore(builder.buildWitnesses("I bought this glass, because it matches those dinner plates.", "I bought those glasses."));
    List<MatchNonMatch> matchNonMatchList = collateCore.doCompareWitnesses(collateCore.getWitness(1), collateCore.getWitness(2));
    collateCore.sortPermutationsByVariation(matchNonMatchList);
    assertEquals("[(1->1), (2->2), (3->3), (4->4)]", matchNonMatchList.get(0).getMatches().toString());
  }
}
