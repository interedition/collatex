package com.sd_editions.collatex.permutations;

import java.util.List;

import org.junit.Test;

import com.sd_editions.collatex.Block.Util;

import eu.interedition.collatex.input.Witness;
import eu.interedition.collatex.input.WitnessBuilder;

/**
 * not really a test case: maybe refactor into the web application 
 */
public class MatchesView {

  private final WitnessBuilder builder = new WitnessBuilder();

  public String renderPermutations(Witness witnessA, Witness witnessB) {
    CollateCore collateCore = new CollateCore(witnessA, witnessB);
    List<MatchNonMatch> matchNonMatches = collateCore.doCompareWitnesses(witnessA, witnessB);
    collateCore.sortPermutationsByVariation(matchNonMatches);

    StringBuilder result = new StringBuilder();
    result.append("Witness A: ").append(witnessA.sentence).append('\n');
    result.append("Witness B: ").append(witnessB.sentence).append('\n');
    for (MatchNonMatch matchNonMatch : matchNonMatches)
      result.append(renderPermutation(matchNonMatch));
    return result.toString();
  }

  private String renderPermutation(MatchNonMatch matchNonMatch) {
    StringBuilder result = new StringBuilder();

    result.append(" * Permutation: ").append(matchNonMatch.getMatches()).append('\n');
    List<MatchSequence> sequencesForBase = matchNonMatch.getMatchSequencesForBase();
    for (MatchSequence matchSequence : sequencesForBase) {
      result.append("    * MatchSequence: ").append(matchSequence).append('\n');
      List<Match> matches = matchSequence.getMatches();
      for (Match match : matches) {
        result.append("     * Match: ").append(match);
        result.append(": ").append(match.getBaseWord()).append(" -> ").append(match.getWitnessWord());
        result.append(", word distance: ").append(match.wordDistance).append('\n');
      }
    }

    for (NonMatch nonMatch : matchNonMatch.getNonMatches()) {
      result.append("    * Non-Match: ").append(nonMatch.getBase()).append(" ~> ").append(nonMatch.getWitness());
      result.append('\n');
    }

    result.append("   Summary: ").append(sequencesForBase.size()).append(" match sequences,\t");
    result.append(matchNonMatch.getNonMatches().size()).append(" non-matches,\t");
    result.append(matchNonMatch.getWordDistanceSum()).append(" summarized word distance,\t");
    result.append(matchNonMatch.getVariationMeasure()).append(" variance\n");
    result.append('\n');
    return result.toString();
  }

  @Test
  public void showPermutationsTara() {
    Witness[] witnesses = builder.buildWitnesses("I bought this glass, because it matches those dinner plates.", "I bought those glasses.");
    Util.p(renderPermutations(witnesses[0], witnesses[1]));
  }
}
