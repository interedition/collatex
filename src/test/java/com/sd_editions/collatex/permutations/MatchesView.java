package com.sd_editions.collatex.permutations;

import org.junit.Test;

import com.sd_editions.collatex.Block.Util;

import eu.interedition.collatex.input.Segment;
import eu.interedition.collatex.input.builders.WitnessBuilder;

/**
 * not really a test case: maybe refactor into the web application 
 */
public class MatchesView {

  private final WitnessBuilder builder = new WitnessBuilder();

  public String renderPermutations(Segment witnessA, Segment witnessB) {
    //    CollateCore collateCore = new CollateCore(witnessA, witnessB);
    //    List<MatchNonMatch> matchNonMatches = collateCore.doCompareWitnesses(witnessA, witnessB);
    //    collateCore.sortPermutationsByVariation(matchNonMatches);
    //
    StringBuilder result = new StringBuilder();
    //    result.append("Witness A: ").append(witnessA.sentence).append('\n');
    //    result.append("Witness B: ").append(witnessB.sentence).append('\n');
    //    for (MatchNonMatch matchNonMatch : matchNonMatches)
    //      result.append(renderPermutation(matchNonMatch));
    return result.toString();
  }

  @Test
  public void showPermutationsTara() {
    Segment[] witnesses = builder.buildWitnesses("I bought this glass, because it matches those dinner plates.", "I bought those glasses.");
    Util.p(renderPermutations(witnesses[0], witnesses[1]));
  }
}
