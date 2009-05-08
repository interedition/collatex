package com.sd_editions.collatex.permutations;

import java.util.List;

import junit.framework.TestCase;

import org.junit.Test;

import com.google.common.base.Join;
import com.google.common.collect.Lists;

public class CollateCoreTest extends TestCase {
  private WitnessBuilder builder;

  @Override
  protected void setUp() throws Exception {
    builder = new WitnessBuilder();
    super.setUp();
  }

  @Test
  public void testGetAllMatchUnmatchPermutations() {
    CollateCore cc = new CollateCore(builder.buildWitnesses("The black car", "The dark car", "the dark day"));
    List<List<MatchUnmatch>> allMatchUnmatchPermutations = cc.getAllMatchUnmatchPermutations();
    // there should be 3 witness pairs
    assertEquals(3, allMatchUnmatchPermutations.size());
    List<MatchUnmatch> list = allMatchUnmatchPermutations.get(0);
    assertEquals(1, list.size());
    showAllMatchUnmatchPermutations(allMatchUnmatchPermutations);
  }

  @Test
  public void testGetAllMatchUnmatchPermutations1() {
    CollateCore cc = new CollateCore(builder.buildWitnesses("The cat chases the dog and mouse", "The dog chases the cat and mouse", "the dog and mouse chase the cat"));
    List<List<MatchUnmatch>> allMatchUnmatchPermutations = cc.getAllMatchUnmatchPermutations();
    showAllMatchUnmatchPermutations(allMatchUnmatchPermutations);
    assertEquals(3, allMatchUnmatchPermutations.size());
  }

  void showAllMatchUnmatchPermutations(List<List<MatchUnmatch>> allMatchUnmatchPermutations) {
    for (List<MatchUnmatch> list : allMatchUnmatchPermutations) {
      println("witness pair " + allMatchUnmatchPermutations.indexOf(list));
      for (MatchUnmatch matchUnmatch : list) {
        println("  matchUnmatch " + list.indexOf(matchUnmatch));
        List<MatchSequence> matchSequencesForBase = matchUnmatch.getMatchSequencesForBase();
        List<MisMatch> unmatches = matchUnmatch.getUnmatches();
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
    List<MatchUnmatch> matchUnmatchList = collateCore.doCompareWitnesses(collateCore.getWitness(1), collateCore.getWitness(2));
    collateCore.sortPermutationsByVariation(matchUnmatchList);
    assertEquals("[(1->1), (2->2), (3->3), (4->4)]", matchUnmatchList.get(0).getPermutation().toString());
  }
}
