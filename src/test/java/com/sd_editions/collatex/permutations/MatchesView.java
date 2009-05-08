package com.sd_editions.collatex.permutations;

import java.util.List;

import org.junit.Test;

import com.sd_editions.collatex.Block.Util;

/**
 * not really a test case: maybe refactor into the web application 
 */
public class MatchesView {

  private final WitnessBuilder builder = new WitnessBuilder();

  public String renderPermutations(Witness witnessA, Witness witnessB) {
    CollateCore collateCore = new CollateCore(witnessA, witnessB);
    List<MatchUnmatch> matchUnmatches = collateCore.doCompareWitnesses(witnessA, witnessB);
    collateCore.sortPermutationsByVariation(matchUnmatches);

    StringBuilder result = new StringBuilder();
    result.append("Witness A: ").append(witnessA.sentence).append('\n');
    result.append("Witness B: ").append(witnessB.sentence).append('\n');
    for (MatchUnmatch matchUnmatch : matchUnmatches)
      result.append(renderPermutation(matchUnmatch));
    return result.toString();
  }

  private String renderPermutation(MatchUnmatch matchUnmatch) {
    StringBuilder result = new StringBuilder();

    result.append(" * Permutation: ").append(matchUnmatch.getPermutation()).append('\n');
    List<MatchSequence> sequencesForBase = matchUnmatch.getMatchSequencesForBase();
    for (MatchSequence matchSequence : sequencesForBase) {
      result.append("    * MatchSequence: ").append(matchSequence).append('\n');
      List<Match> matches = matchSequence.getMatches();
      for (Match match : matches) {
        result.append("     * Match: ").append(match);
        result.append(": ").append(match.getBaseWord()).append(" -> ").append(match.getWitnessWord());
        result.append(", word distance: ").append(match.wordDistance).append('\n');
      }
    }

    for (MisMatch unmatch : matchUnmatch.getUnmatches()) {
      result.append("    * Non-Match: ").append(unmatch.getBase()).append(" ~> ").append(unmatch.getWitness());
      result.append('\n');
    }

    result.append("   Summary: ").append(sequencesForBase.size()).append(" match sequences,\t");
    result.append(matchUnmatch.getUnmatches().size()).append(" non-matches,\t");
    result.append(matchUnmatch.getWordDistanceSum()).append(" summarized word distance,\t");
    result.append(matchUnmatch.getVariationMeasure()).append(" variance\n");
    result.append('\n');
    return result.toString();
  }

  @Test
  public void showPermutationsTara() {
    Witness[] witnesses = builder.buildWitnesses("I bought this glass, because it matches those dinner plates.", "I bought those glasses.");
    Util.p(renderPermutations(witnesses[0], witnesses[1]));
  }
}
